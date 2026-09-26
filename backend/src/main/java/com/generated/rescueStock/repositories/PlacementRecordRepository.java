package com.generated.rescueStock.repositories;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import com.generated.rescueStock.constants.PlacementRecordType;
import com.generated.rescueStock.models.PlacementRecord;

@Repository
public class PlacementRecordRepository {
  private final List<PlacementRecord> records = new CopyOnWriteArrayList<>();
  private final AtomicLong sequence = new AtomicLong(0);

  public PlacementRecordRepository() {
    save(new PlacementRecord(null, 1L, PlacementRecordType.RECEIVE.name(), 100, "刘敏", null, 100, "2026-09-24T08:00:00Z"));
    save(new PlacementRecord(null, 1L, PlacementRecordType.RECEIVE.name(), 68, "刘敏", null, 168, "2026-09-25T09:30:00Z"));
    save(new PlacementRecord(null, 2L, PlacementRecordType.RECEIVE.name(), 120, "陈刚", null, 120, "2026-09-24T10:00:00Z"));
  }

  public PlacementRecord save(PlacementRecord record) {
    record.id = sequence.incrementAndGet();
    records.add(record);
    return record;
  }

  public List<PlacementRecord> findRecentByShelter(Long shelterId, int limit) {
    return records.stream()
        .filter(record -> shelterId.equals(record.shelterId))
        .sorted(Comparator.comparing((PlacementRecord record) -> record.id).reversed())
        .limit(limit)
        .toList();
  }
}
