export const PlacementRecordType = ["RECEIVE", "TRANSFER_OUT", "TRANSFER_IN"] as const;
export type PlacementRecordType = (typeof PlacementRecordType)[number];
export const PlacementRecordTypeText: Record<PlacementRecordType, string> = {
  RECEIVE: "接收",
  TRANSFER_OUT: "转出",
  TRANSFER_IN: "转入"
};
