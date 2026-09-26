import { defineStore } from "pinia";
import {
  listShelter,
  listShelterPlacementRecords,
  postShelterReceive,
  postShelterStatus,
  postShelterTransfer,
  postShelterTransferOut
} from "../api/Shelter";
import { ERROR_MESSAGES } from "../constants/errorMessages";
import { LOG_TEMPLATES } from "../constants/logTemplates";
import type { ShelterPlacementType } from "../constants/ShelterPlacementType";
import type { Shelter } from "../types/Shelter";
import type {
  PlacementPayload,
  ShelterPlacementRecord,
  ShelterStatusPayload,
  TransferPayload
} from "../types/ShelterPlacementRecord";

const SHELTER_LOG = LOG_TEMPLATES.Shelter;

function fail(message: string): never {
  throw new Error(message);
}

function assertAmount(amount: number) {
  if (!Number.isInteger(amount) || amount <= 0) fail(ERROR_MESSAGES.INVALID_AMOUNT);
}

function assertOperator(operator: string) {
  if (!operator.trim()) fail(ERROR_MESSAGES.VALIDATION_FAILED);
}

function assertReceivable(shelter: Shelter) {
  // 关闭或停用期间不能接收。
  if (shelter.open_status === "CLOSED" || shelter.open_status === "STANDBY") {
    fail(ERROR_MESSAGES.SHELTER_NOT_OPEN);
  }
}

// 人数变动后联动开放状态：达到核定容量自动满员，人数回落恢复开放。
function applyPopulation(shelter: Shelter, next: number) {
  shelter.current_population = next;
  if (next >= shelter.capacity && shelter.open_status === "OPEN") shelter.open_status = "FULL";
  if (next < shelter.capacity && shelter.open_status === "FULL") shelter.open_status = "OPEN";
}

export const useShelterStore = defineStore("shelter", {
  state: () => ({
    rows: [] as Shelter[],
    records: [] as ShelterPlacementRecord[],
    loading: false
  }),
  getters: {
    remaining: () => (shelter: Shelter) => Math.max(0, shelter.capacity - shelter.current_population),
    recordsOf: (state) => (shelterId: number) => state.records.filter((record) => record.shelter_id === shelterId),
    latestOf: (state) => (shelterId: number) =>
      state.records.find((record) => record.shelter_id === shelterId) ?? null,
    totalPopulation: (state) => state.rows.reduce((sum, row) => sum + row.current_population, 0),
    totalRemaining: (state) =>
      state.rows.reduce((sum, row) => sum + Math.max(0, row.capacity - row.current_population), 0),
    fullCount: (state) => state.rows.filter((row) => row.open_status === "FULL").length
  },
  actions: {
    async load() {
      this.loading = true;
      try {
        const [rows, records] = await Promise.all([listShelter(), listShelterPlacementRecords()]);
        this.rows = rows;
        this.records = [...records].sort((a, b) => b.id - a.id);
      } finally {
        this.loading = false;
      }
    },
    mustFind(shelterId: number): Shelter {
      const shelter = this.rows.find((row) => row.id === shelterId);
      if (!shelter) fail(ERROR_MESSAGES.SHELTER_NOT_FOUND);
      return shelter;
    },
    appendRecord(
      shelter: Shelter,
      changeType: ShelterPlacementType,
      amount: number,
      operator: string,
      before: number,
      counterpartShelterId: number | null,
      remark: string
    ) {
      const nextId = this.records.reduce((max, record) => Math.max(max, record.id), 0) + 1;
      this.records.unshift({
        id: nextId,
        shelter_id: shelter.id,
        change_type: changeType,
        amount,
        operator: operator.trim(),
        before_population: before,
        after_population: shelter.current_population,
        counterpart_shelter_id: counterpartShelterId,
        remark: remark.trim(),
        created_at: new Date().toISOString()
      });
    },
    async receive(payload: PlacementPayload) {
      const shelter = this.mustFind(payload.shelter_id);
      assertAmount(payload.amount);
      assertOperator(payload.operator);
      assertReceivable(shelter);
      if (shelter.current_population + payload.amount > shelter.capacity) {
        fail(ERROR_MESSAGES.CAPACITY_EXCEEDED);
      }
      const before = shelter.current_population;
      applyPopulation(shelter, before + payload.amount);
      this.appendRecord(shelter, "RECEIVE", payload.amount, payload.operator, before, null, payload.remark);
      console.info(SHELTER_LOG[4], shelter.name, `+${payload.amount}`, payload.operator);
      await postShelterReceive(payload);
    },
    async transferOut(payload: PlacementPayload) {
      const shelter = this.mustFind(payload.shelter_id);
      assertAmount(payload.amount);
      assertOperator(payload.operator);
      if (payload.amount > shelter.current_population) fail(ERROR_MESSAGES.INSUFFICIENT_POPULATION);
      const before = shelter.current_population;
      applyPopulation(shelter, before - payload.amount);
      this.appendRecord(shelter, "TRANSFER_OUT", payload.amount, payload.operator, before, null, payload.remark);
      console.info(SHELTER_LOG[5], shelter.name, `-${payload.amount}`, payload.operator);
      await postShelterTransferOut(payload);
    },
    async transfer(payload: TransferPayload) {
      if (payload.from_shelter_id === payload.to_shelter_id) fail(ERROR_MESSAGES.SAME_SHELTER);
      const from = this.mustFind(payload.from_shelter_id);
      const to = this.mustFind(payload.to_shelter_id);
      assertAmount(payload.amount);
      assertOperator(payload.operator);
      // 先完成两边全部校验，任一步失败都不改动人数，保证点间转移原子生效。
      if (payload.amount > from.current_population) fail(ERROR_MESSAGES.INSUFFICIENT_POPULATION);
      assertReceivable(to);
      if (to.current_population + payload.amount > to.capacity) fail(ERROR_MESSAGES.CAPACITY_EXCEEDED);
      const fromBefore = from.current_population;
      const toBefore = to.current_population;
      applyPopulation(from, fromBefore - payload.amount);
      applyPopulation(to, toBefore + payload.amount);
      this.appendRecord(from, "TRANSFER_OUT", payload.amount, payload.operator, fromBefore, to.id, payload.remark);
      this.appendRecord(to, "TRANSFER_IN", payload.amount, payload.operator, toBefore, from.id, payload.remark);
      console.info(SHELTER_LOG[6], from.name, "->", to.name, payload.amount, payload.operator);
      await postShelterTransfer(payload);
    },
    async changeStatus(payload: ShelterStatusPayload) {
      const shelter = this.mustFind(payload.shelter_id);
      assertOperator(payload.operator);
      // 未迁空的避难点拒绝关闭。
      if (payload.open_status === "CLOSED" && shelter.current_population > 0) {
        fail(ERROR_MESSAGES.SHELTER_NOT_EMPTY);
      }
      if (payload.open_status === "OPEN") {
        // 重新开放时按当前人数恢复 OPEN/FULL。
        shelter.open_status = shelter.current_population >= shelter.capacity ? "FULL" : "OPEN";
      } else {
        shelter.open_status = payload.open_status;
      }
      console.info(SHELTER_LOG[2], shelter.name, "->", shelter.open_status, payload.operator);
      await postShelterStatus(payload);
    }
  }
});
