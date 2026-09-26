package com.generated.rescueStock.models;

public class ShelterPlacementRecord {
  public Long id;
  public Long shelterId;
  public String changeType;
  public int amount;
  public String operator;
  public int beforePopulation;
  public int afterPopulation;
  public Long counterpartShelterId;
  public String remark;
  public String createdAt;

  public ShelterPlacementRecord() {
  }

  public ShelterPlacementRecord(Long id, Long shelterId, String changeType, int amount, String operator,
      int beforePopulation, int afterPopulation, Long counterpartShelterId, String remark, String createdAt) {
    this.id = id;
    this.shelterId = shelterId;
    this.changeType = changeType;
    this.amount = amount;
    this.operator = operator;
    this.beforePopulation = beforePopulation;
    this.afterPopulation = afterPopulation;
    this.counterpartShelterId = counterpartShelterId;
    this.remark = remark;
    this.createdAt = createdAt;
  }
}
