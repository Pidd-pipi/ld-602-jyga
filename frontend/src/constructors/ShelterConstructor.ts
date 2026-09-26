import type { Shelter } from "../types/Shelter";
import type { PlacementPayload, ShelterPlacementRecord, TransferPayload } from "../types/ShelterPlacementRecord";

export const createDefaultShelter = (overrides: Partial<Shelter> = {}): Shelter => ({
  id: 0,
  name: "",
  district: "",
  capacity: 0,
  current_population: 0,
  contact_person: "",
  risk_level: "LOW",
  open_status: "STANDBY",
  ...overrides
});

export const createShelterForm = createDefaultShelter;
export const createShelterResponse = createDefaultShelter;

export const createPlacementRecord = (overrides: Partial<ShelterPlacementRecord> = {}): ShelterPlacementRecord => ({
  id: 0,
  shelter_id: 0,
  change_type: "RECEIVE",
  amount: 0,
  operator: "",
  before_population: 0,
  after_population: 0,
  counterpart_shelter_id: null,
  remark: "",
  created_at: "",
  ...overrides
});

export const createReceiveForm = (overrides: Partial<PlacementPayload> = {}): PlacementPayload => ({
  shelter_id: 0,
  amount: 1,
  operator: "",
  remark: "",
  ...overrides
});

export const createTransferForm = (overrides: Partial<TransferPayload> = {}): TransferPayload => ({
  from_shelter_id: 0,
  to_shelter_id: 0,
  amount: 1,
  operator: "",
  remark: "",
  ...overrides
});
