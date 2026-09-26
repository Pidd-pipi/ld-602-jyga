import { ERROR_MESSAGES } from "../constants/errorMessages";
import type { PlacementRecordType } from "../constants/PlacementRecordType";
import type { ShelterStatus } from "../constants/ShelterStatus";
import { mockData } from "../mocks/seedData";
import type { PlacementRecord } from "../types/PlacementRecord";
import type { Shelter } from "../types/Shelter";

const endpoint = "/api/shelter";
const RECENT_LIMIT = 5;

interface ErrorBody {
  code?: string;
  message?: string;
}

async function postOrNull<T>(url: string, payload: unknown): Promise<T | null> {
  try {
    const res = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    if (res.ok) return (await res.json()) as T;
    const body = (await res.json().catch(() => null)) as ErrorBody | null;
    if (body && body.code) throw new Error(body.message ?? ERROR_MESSAGES.REQUEST_FAILED);
    return null;
  } catch (error) {
    // Network failure means the backend is unreachable: fall back to the local mock.
    if (error instanceof TypeError) return null;
    throw error;
  }
}

// ---------------------------------------------------------------------------
// Local mock engine: mirrors the backend rules so offline review keeps working.
// ---------------------------------------------------------------------------

const mockShelters: Shelter[] = (mockData.shelter as unknown as Shelter[]).map((row) => ({
  ...row,
  recent_records: row.recent_records.map((record) => ({ ...record }))
}));
let mockRecordSequence = 1000;

function cloneRows(): Shelter[] {
  return mockShelters.map((row) => ({ ...row, recent_records: row.recent_records.map((record) => ({ ...record })) }));
}

function mockShelter(id: number): Shelter {
  const shelter = mockShelters.find((row) => row.id === id);
  if (!shelter) throw new Error(ERROR_MESSAGES.SHELTER_NOT_FOUND);
  return shelter;
}

function requireCount(count: number): void {
  if (!Number.isInteger(count) || count <= 0) throw new Error(ERROR_MESSAGES.INVALID_COUNT);
}

function requireOperator(operator: string): void {
  if (!operator) throw new Error(ERROR_MESSAGES.INVALID_OPERATOR);
}

function syncSlots(shelter: Shelter): void {
  shelter.remaining_slots = shelter.capacity - shelter.current_population;
}

function reopenIfNotFull(shelter: Shelter): void {
  if (shelter.open_status === "FULL" && shelter.current_population < shelter.capacity) {
    shelter.open_status = "OPEN";
  }
}

function pushRecord(shelter: Shelter, type: PlacementRecordType, count: number, operator: string, relatedId: number | null): void {
  const record: PlacementRecord = {
    id: ++mockRecordSequence,
    shelter_id: shelter.id,
    record_type: type,
    count,
    operator,
    related_shelter_id: relatedId,
    resulting_population: shelter.current_population,
    created_at: new Date().toISOString()
  };
  shelter.recent_records = [record, ...shelter.recent_records].slice(0, RECENT_LIMIT);
}

function mockReceive(id: number, count: number, operator: string): Shelter {
  const shelter = mockShelter(id);
  requireCount(count);
  requireOperator(operator);
  if (shelter.open_status === "CLOSED" || shelter.open_status === "STANDBY") throw new Error(ERROR_MESSAGES.SHELTER_NOT_OPEN);
  if (shelter.current_population + count > shelter.capacity) throw new Error(ERROR_MESSAGES.CAPACITY_EXCEEDED);
  shelter.current_population += count;
  if (shelter.current_population >= shelter.capacity) shelter.open_status = "FULL";
  syncSlots(shelter);
  pushRecord(shelter, "RECEIVE", count, operator, null);
  return { ...shelter };
}

function mockTransferOut(id: number, count: number, operator: string): Shelter {
  const shelter = mockShelter(id);
  requireCount(count);
  requireOperator(operator);
  if (count > shelter.current_population) throw new Error(ERROR_MESSAGES.INSUFFICIENT_POPULATION);
  shelter.current_population -= count;
  reopenIfNotFull(shelter);
  syncSlots(shelter);
  pushRecord(shelter, "TRANSFER_OUT", count, operator, null);
  return { ...shelter };
}

function mockTransfer(fromId: number, toId: number, count: number, operator: string): Shelter[] {
  if (fromId === toId) throw new Error(ERROR_MESSAGES.SAME_SHELTER);
  const from = mockShelter(fromId);
  const to = mockShelter(toId);
  requireCount(count);
  requireOperator(operator);
  // Validate everything before mutating so a failed transfer changes nothing.
  if (count > from.current_population) throw new Error(ERROR_MESSAGES.INSUFFICIENT_POPULATION);
  if (to.open_status === "CLOSED" || to.open_status === "STANDBY") throw new Error(ERROR_MESSAGES.SHELTER_NOT_OPEN);
  if (to.current_population + count > to.capacity) throw new Error(ERROR_MESSAGES.CAPACITY_EXCEEDED);
  from.current_population -= count;
  reopenIfNotFull(from);
  to.current_population += count;
  if (to.current_population >= to.capacity) to.open_status = "FULL";
  syncSlots(from);
  syncSlots(to);
  pushRecord(from, "TRANSFER_OUT", count, operator, to.id);
  pushRecord(to, "TRANSFER_IN", count, operator, from.id);
  return [{ ...from }, { ...to }];
}

function mockUpdateStatus(id: number, status: ShelterStatus, operator: string): Shelter {
  const shelter = mockShelter(id);
  if (!["CLOSED", "STANDBY", "OPEN", "FULL"].includes(status)) throw new Error(ERROR_MESSAGES.INVALID_STATUS);
  if (status === "CLOSED" && shelter.current_population > 0) throw new Error(ERROR_MESSAGES.SHELTER_NOT_EMPTY);
  shelter.open_status = status === "OPEN" && shelter.current_population >= shelter.capacity ? "FULL" : status;
  syncSlots(shelter);
  void operator;
  return { ...shelter };
}

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

export async function listShelter(): Promise<Shelter[]> {
  if (typeof fetch !== "undefined" && endpoint.startsWith("/api")) {
    try {
      const res = await fetch(endpoint);
      if (res.ok) return await res.json();
    } catch {
      // Local mock fallback keeps the UI available during offline review.
    }
  }
  return cloneRows();
}

export async function receiveShelter(id: number, count: number, operator: string): Promise<Shelter> {
  const remote = await postOrNull<Shelter>(`${endpoint}/${id}/receive`, { count, operator });
  return remote ?? mockReceive(id, count, operator);
}

export async function transferOutShelter(id: number, count: number, operator: string): Promise<Shelter> {
  const remote = await postOrNull<Shelter>(`${endpoint}/${id}/transfer-out`, { count, operator });
  return remote ?? mockTransferOut(id, count, operator);
}

export async function transferShelter(fromId: number, toId: number, count: number, operator: string): Promise<Shelter[]> {
  const remote = await postOrNull<Shelter[]>(`${endpoint}/transfer`, { fromId, toId, count, operator });
  return remote ?? mockTransfer(fromId, toId, count, operator);
}

export async function updateShelterStatus(id: number, status: ShelterStatus, operator: string): Promise<Shelter> {
  const remote = await postOrNull<Shelter>(`${endpoint}/${id}/status`, { status, operator });
  return remote ?? mockUpdateStatus(id, status, operator);
}

export async function saveShelter(payload: Shelter) {
  console.info("save Shelter", payload);
  return payload;
}
