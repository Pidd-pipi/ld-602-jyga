import type { ShelterPlacementType } from "../constants/ShelterPlacementType";
import type { ShelterStatus } from "../constants/ShelterStatus";

export interface ShelterPlacementRecord {
  id: number;
  shelter_id: number;
  change_type: ShelterPlacementType;
  amount: number;
  operator: string;
  before_population: number;
  after_population: number;
  counterpart_shelter_id: number | null;
  remark: string;
  created_at: string;
}

export interface PlacementPayload {
  shelter_id: number;
  amount: number;
  operator: string;
  remark: string;
}

export interface TransferPayload {
  from_shelter_id: number;
  to_shelter_id: number;
  amount: number;
  operator: string;
  remark: string;
}

export interface ShelterStatusPayload {
  shelter_id: number;
  open_status: ShelterStatus;
  operator: string;
}
