package com.generated.rescueStock.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.generated.rescueStock.constants.ErrorCodes;
import com.generated.rescueStock.constants.ErrorMessages;
import com.generated.rescueStock.constants.LogTemplates;
import com.generated.rescueStock.constants.PlacementRecordType;
import com.generated.rescueStock.constants.ShelterStatus;
import com.generated.rescueStock.constructors.ShelterDtoFactory;
import com.generated.rescueStock.models.PlacementRecord;
import com.generated.rescueStock.models.Shelter;
import com.generated.rescueStock.repositories.PlacementRecordRepository;
import com.generated.rescueStock.repositories.ShelterRepository;

@Service
public class ShelterService {
  private static final Logger log = LoggerFactory.getLogger(ShelterService.class);
  private static final int RECENT_LIMIT = 5;

  private final ShelterRepository repo;
  private final PlacementRecordRepository records;

  public ShelterService(ShelterRepository repo, PlacementRecordRepository records) {
    this.repo = repo;
    this.records = records;
  }

  public List<Map<String, Object>> list() {
    List<Map<String, Object>> rows = new ArrayList<>();
    for (Shelter shelter : repo.findAll()) {
      rows.add(dto(shelter));
    }
    return rows;
  }

  public synchronized Map<String, Object> receive(Long id, Integer count, String operator) {
    Shelter shelter = requireShelter(id);
    int amount = requireCount(count);
    String operatorName = requireOperator(operator);
    if (ShelterStatus.CLOSED.name().equals(shelter.status) || ShelterStatus.STANDBY.name().equals(shelter.status)) {
      throw new ServiceException(ErrorCodes.SHELTER_NOT_OPEN, ErrorMessages.SHELTER_NOT_OPEN);
    }
    if (shelter.currentPopulation + amount > shelter.capacity) {
      throw new ServiceException(ErrorCodes.CAPACITY_EXCEEDED, ErrorMessages.CAPACITY_EXCEEDED);
    }
    shelter.currentPopulation += amount;
    if (shelter.currentPopulation >= shelter.capacity) {
      shelter.status = ShelterStatus.FULL.name();
    }
    repo.save(shelter);
    record(shelter, PlacementRecordType.RECEIVE, amount, operatorName, null);
    log.info("{} shelter={} count={} operator={}", LogTemplates.RECEIVE, shelter.id, amount, operatorName);
    return dto(shelter);
  }

  public synchronized Map<String, Object> transferOut(Long id, Integer count, String operator) {
    Shelter shelter = requireShelter(id);
    int amount = requireCount(count);
    String operatorName = requireOperator(operator);
    if (amount > shelter.currentPopulation) {
      throw new ServiceException(ErrorCodes.INSUFFICIENT_POPULATION, ErrorMessages.INSUFFICIENT_POPULATION);
    }
    shelter.currentPopulation -= amount;
    reopenIfNotFull(shelter);
    repo.save(shelter);
    record(shelter, PlacementRecordType.TRANSFER_OUT, amount, operatorName, null);
    log.info("{} shelter={} count={} operator={}", LogTemplates.TRANSFER_OUT, shelter.id, amount, operatorName);
    return dto(shelter);
  }

  public synchronized List<Map<String, Object>> transfer(Long fromId, Long toId, Integer count, String operator) {
    if (fromId == null || toId == null || fromId.equals(toId)) {
      throw new ServiceException(ErrorCodes.SAME_SHELTER, ErrorMessages.SAME_SHELTER);
    }
    Shelter from = requireShelter(fromId);
    Shelter to = requireShelter(toId);
    int amount = requireCount(count);
    String operatorName = requireOperator(operator);
    // 先完成全部校验再改动人数：任一步失败，两边人数都不变
    if (amount > from.currentPopulation) {
      throw new ServiceException(ErrorCodes.INSUFFICIENT_POPULATION, ErrorMessages.INSUFFICIENT_POPULATION);
    }
    if (ShelterStatus.CLOSED.name().equals(to.status) || ShelterStatus.STANDBY.name().equals(to.status)) {
      throw new ServiceException(ErrorCodes.SHELTER_NOT_OPEN, ErrorMessages.SHELTER_NOT_OPEN);
    }
    if (to.currentPopulation + amount > to.capacity) {
      throw new ServiceException(ErrorCodes.CAPACITY_EXCEEDED, ErrorMessages.CAPACITY_EXCEEDED);
    }
    from.currentPopulation -= amount;
    reopenIfNotFull(from);
    to.currentPopulation += amount;
    if (to.currentPopulation >= to.capacity) {
      to.status = ShelterStatus.FULL.name();
    }
    repo.save(from);
    repo.save(to);
    record(from, PlacementRecordType.TRANSFER_OUT, amount, operatorName, to.id);
    record(to, PlacementRecordType.TRANSFER_IN, amount, operatorName, from.id);
    log.info("{} from={} to={} count={} operator={}", LogTemplates.TRANSFER, from.id, to.id, amount, operatorName);
    return List.of(dto(from), dto(to));
  }

  public synchronized Map<String, Object> updateStatus(Long id, String status, String operator) {
    Shelter shelter = requireShelter(id);
    String target = parseStatus(status);
    if (ShelterStatus.CLOSED.name().equals(target) && shelter.currentPopulation > 0) {
      throw new ServiceException(ErrorCodes.SHELTER_NOT_EMPTY, ErrorMessages.SHELTER_NOT_EMPTY);
    }
    if (ShelterStatus.OPEN.name().equals(target) && shelter.currentPopulation >= shelter.capacity) {
      shelter.status = ShelterStatus.FULL.name();
    } else {
      shelter.status = target;
    }
    repo.save(shelter);
    log.info("{} shelter={} status={} operator={}", LogTemplates.STATUS, shelter.id, shelter.status, operator);
    return dto(shelter);
  }

  private Shelter requireShelter(Long id) {
    if (id == null) {
      throw new ServiceException(ErrorCodes.SHELTER_NOT_FOUND, ErrorMessages.SHELTER_NOT_FOUND);
    }
    return repo.findById(id)
        .orElseThrow(() -> new ServiceException(ErrorCodes.SHELTER_NOT_FOUND, ErrorMessages.SHELTER_NOT_FOUND));
  }

  private int requireCount(Integer count) {
    if (count == null || count <= 0) {
      throw new ServiceException(ErrorCodes.INVALID_COUNT, ErrorMessages.INVALID_COUNT);
    }
    return count;
  }

  private String requireOperator(String operator) {
    if (operator == null || operator.isBlank()) {
      throw new ServiceException(ErrorCodes.INVALID_OPERATOR, ErrorMessages.INVALID_OPERATOR);
    }
    return operator.trim();
  }

  private String parseStatus(String status) {
    if (status != null) {
      try {
        return ShelterStatus.valueOf(status.trim().toUpperCase()).name();
      } catch (IllegalArgumentException ignored) {
        // fall through to the uniform invalid-status error below
      }
    }
    throw new ServiceException(ErrorCodes.INVALID_STATUS, ErrorMessages.INVALID_STATUS);
  }

  private void reopenIfNotFull(Shelter shelter) {
    if (ShelterStatus.FULL.name().equals(shelter.status) && shelter.currentPopulation < shelter.capacity) {
      shelter.status = ShelterStatus.OPEN.name();
    }
  }

  private void record(Shelter shelter, PlacementRecordType type, int count, String operator, Long relatedShelterId) {
    records.save(new PlacementRecord(null, shelter.id, type.name(), count, operator,
        relatedShelterId, shelter.currentPopulation, Instant.now().toString()));
  }

  private Map<String, Object> dto(Shelter shelter) {
    return ShelterDtoFactory.create(shelter, records.findRecentByShelter(shelter.id, RECENT_LIMIT));
  }
}
