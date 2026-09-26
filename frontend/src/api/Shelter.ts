import { mockData } from "../mocks/seedData";
import type { Shelter } from "../types/Shelter";
import type {
  PlacementPayload,
  ShelterPlacementRecord,
  ShelterStatusPayload,
  TransferPayload
} from "../types/ShelterPlacementRecord";

const endpoint = "/api/shelter";
const jsonHeaders = { "Content-Type": "application/json" };

export async function listShelter(): Promise<Shelter[]> {
  try {
    const res = await fetch(endpoint);
    if (res.ok) return await res.json();
  } catch {
    // Local mock fallback keeps the UI available during offline review.
  }
  return [...(mockData.shelter as unknown as Shelter[])];
}

export async function listShelterPlacementRecords(): Promise<ShelterPlacementRecord[]> {
  try {
    const res = await fetch(`${endpoint}/records`);
    if (res.ok) return await res.json();
  } catch {
    // Local mock fallback keeps the UI available during offline review.
  }
  return [...(mockData.shelterPlacementRecord as unknown as ShelterPlacementRecord[])];
}

async function postPlacement(path: string, payload: unknown): Promise<boolean> {
  try {
    const res = await fetch(`${endpoint}${path}`, {
      method: "POST",
      headers: jsonHeaders,
      body: JSON.stringify(payload)
    });
    return res.ok;
  } catch {
    // Offline review: the store keeps the local mutation as source of truth.
    return false;
  }
}

export const postShelterReceive = (payload: PlacementPayload) => postPlacement("/receive", payload);
export const postShelterTransferOut = (payload: PlacementPayload) => postPlacement("/transfer-out", payload);
export const postShelterTransfer = (payload: TransferPayload) => postPlacement("/transfer", payload);
export const postShelterStatus = (payload: ShelterStatusPayload) => postPlacement("/status", payload);

export async function saveShelter(payload: Shelter) {
  console.info("save Shelter", payload);
  return payload;
}
