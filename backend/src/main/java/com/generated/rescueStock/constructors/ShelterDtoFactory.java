package com.generated.rescueStock.constructors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.generated.rescueStock.models.PlacementRecord;
import com.generated.rescueStock.models.Shelter;

public final class ShelterDtoFactory {
  private ShelterDtoFactory() {}

  public static Map<String, Object> create(Shelter shelter, List<PlacementRecord> recentRecords) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", shelter.id);
    dto.put("name", shelter.name);
    dto.put("district", shelter.district);
    dto.put("capacity", shelter.capacity);
    dto.put("current_population", shelter.currentPopulation);
    dto.put("remaining_slots", shelter.capacity - shelter.currentPopulation);
    dto.put("contact_person", shelter.contactPerson);
    dto.put("risk_level", shelter.riskLevel);
    dto.put("open_status", shelter.status);
    List<Map<String, Object>> rows = new ArrayList<>();
    for (PlacementRecord record : recentRecords) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("id", record.id);
      row.put("shelter_id", record.shelterId);
      row.put("record_type", record.recordType);
      row.put("count", record.count);
      row.put("operator", record.operator);
      row.put("related_shelter_id", record.relatedShelterId);
      row.put("resulting_population", record.resultingPopulation);
      row.put("created_at", record.createdAt);
      rows.add(row);
    }
    dto.put("recent_records", rows);
    return dto;
  }
}
