import type { Shelter } from "../types/Shelter";

export const createDefaultShelter = (overrides: Partial<Shelter> = {}): Shelter => ({
  id: 0,
  name: "",
  district: "",
  capacity: 0,
  current_population: 0,
  remaining_slots: 0,
  contact_person: "",
  risk_level: "LOW",
  open_status: "OPEN",
  recent_records: [],
  ...overrides
});

export const createShelterForm = createDefaultShelter;
export const createShelterResponse = createDefaultShelter;
