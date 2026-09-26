package com.generated.rescueStock.models;

public class Shelter {
  public Long id;
  public String name;
  public String district;
  public int capacity;
  public int currentPopulation;
  public String contactPerson;
  public String riskLevel;
  public String openStatus;

  public Shelter() {
  }

  public Shelter(Long id, String name, String district, int capacity, int currentPopulation,
      String contactPerson, String riskLevel, String openStatus) {
    this.id = id;
    this.name = name;
    this.district = district;
    this.capacity = capacity;
    this.currentPopulation = currentPopulation;
    this.contactPerson = contactPerson;
    this.riskLevel = riskLevel;
    this.openStatus = openStatus;
  }
}
