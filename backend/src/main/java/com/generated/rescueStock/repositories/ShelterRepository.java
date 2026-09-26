package com.generated.rescueStock.repositories;

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
  private final Map<Long, Shelter> store = new ConcurrentHashMap<>();

  public ShelterRepository() {
    save(new Shelter(1L, "城东体育馆安置点", "城东区", 500, 320, "王敏", "LOW", ShelterStatus.OPEN.name()));
    save(new Shelter(2L, "滨河学校安置点", "滨河区", 300, 300, "李强", "MEDIUM", ShelterStatus.FULL.name()));
    save(new Shelter(3L, "西山社区中心", "西城区", 150, 0, "赵蕾", "HIGH", ShelterStatus.STANDBY.name()));
    save(new Shelter(4L, "老港仓库改建安置点", "港湾区", 200, 0, "陈刚", "MEDIUM", ShelterStatus.CLOSED.name()));
  }

  public List<Shelter> findAll() {
    return store.values().stream().sorted(Comparator.comparing(shelter -> shelter.id)).toList();
  }

  public Optional<Shelter> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  public Shelter save(Shelter shelter) {
    store.put(shelter.id, shelter);
    return shelter;
  }
}
