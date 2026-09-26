package com.generated.rescueStock.services;

public class ShelterServiceException extends RuntimeException {
  private final String code;

  public ShelterServiceException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String code() {
    return code;
  }
}
