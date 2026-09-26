import type { PlacementRecordType } from "../constants/PlacementRecordType";

export interface PlacementRecord {
  id: number;
  shelter_id: number;
  record_type: PlacementRecordType;
  count: number;
  operator: string;
  related_shelter_id: number | null;
  resulting_population: number;
  created_at: string;
}
