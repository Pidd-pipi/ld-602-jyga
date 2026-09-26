package com.generated.rescueStock.constructors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.generated.rescueStock.models.Shelter;
import com.generated.rescueStock.models.ShelterPlacementRecord;

public final class ShelterDtoFactory {

  private ShelterDtoFactory() {
  }

  public static Map<String, Object> toResponse(Shelter shelter, List<ShelterPlacementRecord> recentRecords) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", shelter.id);
    dto.put("name", shelter.name);
    dto.put("district", shelter.district);
    dto.put("capacity", shelter.capacity);
    dto.put("current_population", shelter.currentPopulation);
    dto.put("remaining_capacity", Math.max(0, shelter.capacity - shelter.currentPopulation));
    dto.put("contact_person", shelter.contactPerson);
    dto.put("risk_level", shelter.riskLevel);
    dto.put("open_status", shelter.openStatus);
    dto.put("recent_records", recentRecords.stream().map(ShelterDtoFactory::toRecordResponse).toList());
    return dto;
  }

  public static Map<String, Object> toRecordResponse(ShelterPlacementRecord record) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", record.id);
    dto.put("shelter_id", record.shelterId);
    dto.put("change_type", record.changeType);
    dto.put("amount", record.amount);
    dto.put("operator", record.operator);
    dto.put("before_population", record.beforePopulation);
    dto.put("after_population", record.afterPopulation);
    dto.put("counterpart_shelter_id", record.counterpartShelterId);
    dto.put("remark", record.remark);
    dto.put("created_at", record.createdAt);
    return dto;
  }
}
