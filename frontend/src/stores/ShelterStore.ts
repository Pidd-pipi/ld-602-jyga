import { defineStore } from "pinia";
import { listShelter, receiveShelter, transferOutShelter, transferShelter, updateShelterStatus } from "../api/Shelter";
import type { ShelterStatus } from "../constants/ShelterStatus";
import type { Shelter } from "../types/Shelter";

export const useShelterStore = defineStore("shelter", {
  state: () => ({ rows: [] as Shelter[], loading: false, error: "" }),
  actions: {
    async load() {
      this.loading = true;
      try {
        this.rows = await listShelter();
      } finally {
        this.loading = false;
      }
    },
    async receive(id: number, count: number, operator: string): Promise<boolean> {
      return this.run(() => receiveShelter(id, count, operator));
    },
    async transferOut(id: number, count: number, operator: string): Promise<boolean> {
      return this.run(() => transferOutShelter(id, count, operator));
    },
    async transfer(fromId: number, toId: number, count: number, operator: string): Promise<boolean> {
      return this.run(() => transferShelter(fromId, toId, count, operator));
    },
    async changeStatus(id: number, status: ShelterStatus, operator: string): Promise<boolean> {
      return this.run(() => updateShelterStatus(id, status, operator));
    },
    async run(action: () => Promise<unknown>): Promise<boolean> {
      this.error = "";
      try {
        await action();
        await this.load();
        return true;
      } catch (cause) {
        this.error = cause instanceof Error ? cause.message : String(cause);
        return false;
      }
    }
  }
});
