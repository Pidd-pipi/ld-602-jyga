package com.generated.rescueStock.models;

public class PlacementRecord {
  public Long id;
  public Long shelterId;
  public String recordType;
  public int count;
  public String operator;
  public Long relatedShelterId;
  public int resultingPopulation;
  public String createdAt;

  public PlacementRecord() {}

  public PlacementRecord(Long id, Long shelterId, String recordType, int count, String operator,
                         Long relatedShelterId, int resultingPopulation, String createdAt) {
    this.id = id;
    this.shelterId = shelterId;
    this.recordType = recordType;
    this.count = count;
    this.operator = operator;
    this.relatedShelterId = relatedShelterId;
    this.resultingPopulation = resultingPopulation;
    this.createdAt = createdAt;
  }
}
