package com.generated.rescueStock.types;

public record ShelterPayload(String payload) {
  public record Placement(Long shelterId, Integer amount, String operator, String remark) {
  }

  public record Transfer(Long fromShelterId, Long toShelterId, Integer amount, String operator, String remark) {
  }

  public record StatusChange(Long shelterId, String openStatus, String operator) {
  }
}
