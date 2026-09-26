import type { ShelterStatus } from "./ShelterStatus";
import type { PlacementRecord } from "./PlacementRecord";

export interface Shelter {
  id: number;
  name: string;
  district: string;
  capacity: number;
  current_population: number;
  remaining_slots: number;
  contact_person: string;
  risk_level: string;
  open_status: ShelterStatus;
  recent_records: PlacementRecord[];
}
