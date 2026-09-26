package com.generated.rescueStock.repositories;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.generated.rescueStock.constants.ShelterPlacementType;
import com.generated.rescueStock.models.ShelterPlacementRecord;

@Repository
public class ShelterPlacementRecordRepository {
  private final List<ShelterPlacementRecord> records = new CopyOnWriteArrayList<>();
  private final AtomicLong sequence = new AtomicLong(0);

  public ShelterPlacementRecordRepository() {
    save(new ShelterPlacementRecord(null, 1L, ShelterPlacementType.RECEIVE.name(), 200, "王敏", 0, 200, null, "台风“海燕”首轮转移安置", "2026-09-24T08:30:00Z"));
    save(new ShelterPlacementRecord(null, 2L, ShelterPlacementType.RECEIVE.name(), 270, "李强", 0, 270, null, "滨河低洼区整体转移", "2026-09-24T10:10:00Z"));
    save(new ShelterPlacementRecord(null, 1L, ShelterPlacementType.RECEIVE.name(), 150, "王敏", 200, 350, null, "第二轮转移安置", "2026-09-24T15:40:00Z"));
    save(new ShelterPlacementRecord(null, 3L, ShelterPlacementType.TRANSFER_OUT.name(), 60, "赵蕾", 60, 0, null, "点位停用，存量人员转出", "2026-09-25T09:20:00Z"));
    save(new ShelterPlacementRecord(null, 1L, ShelterPlacementType.TRANSFER_OUT.name(), 30, "王敏", 350, 320, 2L, "分流至滨河学校安置点", "2026-09-25T11:05:00Z"));
    save(new ShelterPlacementRecord(null, 2L, ShelterPlacementType.TRANSFER_IN.name(), 30, "李强", 270, 300, 1L, "自城东体育馆安置点分流", "2026-09-25T11:05:00Z"));
  }

  public ShelterPlacementRecord save(ShelterPlacementRecord record) {
    if (record.id == null) {
      record.id = sequence.incrementAndGet();
    }
    records.add(record);
    return record;
  }

  public List<ShelterPlacementRecord> findAll() {
    return records.stream()
        .sorted(Comparator.comparing((ShelterPlacementRecord record) -> record.id).reversed())
        .toList();
  }

  public List<ShelterPlacementRecord> findByShelterId(Long shelterId) {
    return findAll().stream().filter(record -> record.shelterId.equals(shelterId)).toList();
  }

  public List<ShelterPlacementRecord> findRecentByShelterId(Long shelterId, int limit) {
    return findByShelterId(shelterId).stream().limit(limit).toList();
  }
}
