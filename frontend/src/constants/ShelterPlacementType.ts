export const ShelterPlacementType = ["RECEIVE", "TRANSFER_OUT", "TRANSFER_IN"] as const;
export type ShelterPlacementType = (typeof ShelterPlacementType)[number];
export const ShelterPlacementTypeText: Record<ShelterPlacementType, string> = {
  RECEIVE: "接收",
  TRANSFER_OUT: "转出",
  TRANSFER_IN: "转入"
};
