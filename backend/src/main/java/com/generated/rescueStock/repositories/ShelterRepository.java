package com.generated.rescueStock.repositories;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import com.generated.rescueStock.constants.ShelterStatus;
import com.generated.rescueStock.models.Shelter;

@Repository
public class ShelterRepository {
  private final Map<Long, Shelter> shelters = new ConcurrentHashMap<>();

  public ShelterRepository() {
    save(new Shelter(1L, "城东体育馆安置点", "城东区", 200, 168, "王建国", "LOW", ShelterStatus.OPEN.name()));
    save(new Shelter(2L, "滨河学校安置点", "滨河区", 120, 120, "李秀兰", "MEDIUM", ShelterStatus.FULL.name()));
    save(new Shelter(3L, "西山社区安置点", "西山区", 80, 0, "赵铁柱", "HIGH", ShelterStatus.STANDBY.name()));
  }

  public List<Shelter> findAll() {
    List<Shelter> all = new ArrayList<>(shelters.values());
    all.sort(Comparator.comparing(shelter -> shelter.id));
    return all;
  }

  public Optional<Shelter> findById(Long id) {
    return Optional.ofNullable(shelters.get(id));
  }

  public Shelter save(Shelter shelter) {
    shelters.put(shelter.id, shelter);
    return shelter;
  }
}
