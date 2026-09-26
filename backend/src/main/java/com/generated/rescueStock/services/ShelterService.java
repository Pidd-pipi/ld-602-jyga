package com.generated.rescueStock.services;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.generated.rescueStock.constants.ErrorCodes;
import com.generated.rescueStock.constants.ErrorMessages;
import com.generated.rescueStock.constants.LogTemplates;
import com.generated.rescueStock.constants.ShelterPlacementType;
import com.generated.rescueStock.constants.ShelterStatus;
import com.generated.rescueStock.constructors.ShelterDtoFactory;
import com.generated.rescueStock.models.Shelter;
import com.generated.rescueStock.models.ShelterPlacementRecord;
import com.generated.rescueStock.repositories.ShelterPlacementRecordRepository;
import com.generated.rescueStock.repositories.ShelterRepository;
import com.generated.rescueStock.types.ShelterPayload;

@Service
public class ShelterService {
  private static final int RECENT_LIMIT = 3;

  private final ShelterRepository repo;
  private final ShelterPlacementRecordRepository recordRepo;

  public ShelterService(ShelterRepository repo, ShelterPlacementRecordRepository recordRepo) {
    this.repo = repo;
    this.recordRepo = recordRepo;
  }

  public List<Map<String, Object>> list() {
    return repo.findAll().stream()
        .map(shelter -> ShelterDtoFactory.toResponse(shelter, recordRepo.findRecentByShelterId(shelter.id, RECENT_LIMIT)))
        .toList();
  }

  public List<Map<String, Object>> records(Long shelterId) {
    List<ShelterPlacementRecord> found = shelterId == null
        ? recordRepo.findAll()
        : recordRepo.findByShelterId(shelterId);
    return found.stream().map(ShelterDtoFactory::toRecordResponse).toList();
  }

  public synchronized Map<String, Object> receive(ShelterPayload.Placement payload) {
    mustValidPayload(payload);
    Shelter shelter = mustFind(payload.shelterId());
    int amount = mustValidAmount(payload.amount());
    mustHaveOperator(payload.operator());
    // 关闭或停用期间不能接收。
    if (ShelterStatus.CLOSED.name().equals(shelter.openStatus) || ShelterStatus.STANDBY.name().equals(shelter.openStatus)) {
      throw new ShelterServiceException(ErrorCodes.SHELTER_NOT_OPEN, ErrorMessages.SHELTER_NOT_OPEN);
    }
    if (shelter.currentPopulation + amount > shelter.capacity) {
      throw new ShelterServiceException(ErrorCodes.CAPACITY_EXCEEDED, ErrorMessages.CAPACITY_EXCEEDED);
    }
    int before = shelter.currentPopulation;
    applyPopulation(shelter, before + amount);
    repo.save(shelter);
    recordRepo.save(newRecord(shelter, ShelterPlacementType.RECEIVE, amount, payload.operator(), before, null, payload.remark()));
    System.out.println(LogTemplates.SHELTER_RECEIVE + " " + shelter.name + " +" + amount + " by " + payload.operator());
    return ShelterDtoFactory.toResponse(shelter, recordRepo.findRecentByShelterId(shelter.id, RECENT_LIMIT));
  }

  public synchronized Map<String, Object> transferOut(ShelterPayload.Placement payload) {
    mustValidPayload(payload);
    Shelter shelter = mustFind(payload.shelterId());
    int amount = mustValidAmount(payload.amount());
    mustHaveOperator(payload.operator());
    if (amount > shelter.currentPopulation) {
      throw new ShelterServiceException(ErrorCodes.INSUFFICIENT_POPULATION, ErrorMessages.INSUFFICIENT_POPULATION);
    }
    int before = shelter.currentPopulation;
    applyPopulation(shelter, before - amount);
    repo.save(shelter);
    recordRepo.save(newRecord(shelter, ShelterPlacementType.TRANSFER_OUT, amount, payload.operator(), before, null, payload.remark()));
    System.out.println(LogTemplates.SHELTER_TRANSFER_OUT + " " + shelter.name + " -" + amount + " by " + payload.operator());
    return ShelterDtoFactory.toResponse(shelter, recordRepo.findRecentByShelterId(shelter.id, RECENT_LIMIT));
  }

  public synchronized Map<String, Object> transfer(ShelterPayload.Transfer payload) {
    mustValidPayload(payload);
    if (payload.fromShelterId() != null && payload.fromShelterId().equals(payload.toShelterId())) {
      throw new ShelterServiceException(ErrorCodes.SAME_SHELTER, ErrorMessages.SAME_SHELTER);
    }
    Shelter from = mustFind(payload.fromShelterId());
    Shelter to = mustFind(payload.toShelterId());
    int amount = mustValidAmount(payload.amount());
    mustHaveOperator(payload.operator());
    // 先完成两边全部校验，任一步失败都不改动人数，保证点间转移原子生效。
    if (amount > from.currentPopulation) {
      throw new ShelterServiceException(ErrorCodes.INSUFFICIENT_POPULATION, ErrorMessages.INSUFFICIENT_POPULATION);
    }
    if (ShelterStatus.CLOSED.name().equals(to.openStatus) || ShelterStatus.STANDBY.name().equals(to.openStatus)) {
      throw new ShelterServiceException(ErrorCodes.SHELTER_NOT_OPEN, ErrorMessages.SHELTER_NOT_OPEN);
    }
    if (to.currentPopulation + amount > to.capacity) {
      throw new ShelterServiceException(ErrorCodes.CAPACITY_EXCEEDED, ErrorMessages.CAPACITY_EXCEEDED);
    }
    int fromBefore = from.currentPopulation;
    int toBefore = to.currentPopulation;
    applyPopulation(from, fromBefore - amount);
    applyPopulation(to, toBefore + amount);
    repo.save(from);
    repo.save(to);
    recordRepo.save(newRecord(from, ShelterPlacementType.TRANSFER_OUT, amount, payload.operator(), fromBefore, to.id, payload.remark()));
    recordRepo.save(newRecord(to, ShelterPlacementType.TRANSFER_IN, amount, payload.operator(), toBefore, from.id, payload.remark()));
    System.out.println(LogTemplates.SHELTER_TRANSFER + " " + from.name + " -> " + to.name + " " + amount + " by " + payload.operator());
    return Map.of(
        "from", ShelterDtoFactory.toResponse(from, recordRepo.findRecentByShelterId(from.id, RECENT_LIMIT)),
        "to", ShelterDtoFactory.toResponse(to, recordRepo.findRecentByShelterId(to.id, RECENT_LIMIT)));
  }

  public synchronized Map<String, Object> changeStatus(ShelterPayload.StatusChange payload) {
    mustValidPayload(payload);
    Shelter shelter = mustFind(payload.shelterId());
    mustHaveOperator(payload.operator());
    String target = payload.openStatus();
    boolean known = false;
    for (ShelterStatus status : ShelterStatus.values()) {
      if (status.name().equals(target)) {
        known = true;
        break;
      }
    }
    if (!known) {
      throw new ShelterServiceException(ErrorCodes.VALIDATION_FAILED, ErrorMessages.VALIDATION_FAILED);
    }
    // 未迁空的避难点拒绝关闭。
    if (ShelterStatus.CLOSED.name().equals(target) && shelter.currentPopulation > 0) {
      throw new ShelterServiceException(ErrorCodes.SHELTER_NOT_EMPTY, ErrorMessages.SHELTER_NOT_EMPTY);
    }
    if (ShelterStatus.OPEN.name().equals(target)) {
      // 重新开放时按当前人数恢复 OPEN/FULL。
      shelter.openStatus = shelter.currentPopulation >= shelter.capacity ? ShelterStatus.FULL.name() : ShelterStatus.OPEN.name();
    } else {
      shelter.openStatus = target;
    }
    repo.save(shelter);
    System.out.println(LogTemplates.SHELTER_STATUS + " " + shelter.name + " -> " + shelter.openStatus + " by " + payload.operator());
    return ShelterDtoFactory.toResponse(shelter, recordRepo.findRecentByShelterId(shelter.id, RECENT_LIMIT));
  }

  private Shelter mustFind(Long id) {
    if (id == null) {
      throw new ShelterServiceException(ErrorCodes.SHELTER_NOT_FOUND, ErrorMessages.SHELTER_NOT_FOUND);
    }
    return repo.findById(id)
        .orElseThrow(() -> new ShelterServiceException(ErrorCodes.SHELTER_NOT_FOUND, ErrorMessages.SHELTER_NOT_FOUND));
  }

  private void mustValidPayload(Object payload) {
    if (payload == null) {
      throw new ShelterServiceException(ErrorCodes.VALIDATION_FAILED, ErrorMessages.VALIDATION_FAILED);
    }
  }

  private int mustValidAmount(Integer amount) {
    if (amount == null || amount <= 0) {
      throw new ShelterServiceException(ErrorCodes.INVALID_AMOUNT, ErrorMessages.INVALID_AMOUNT);
    }
    return amount;
  }

  private void mustHaveOperator(String operator) {
    if (operator == null || operator.isBlank()) {
      throw new ShelterServiceException(ErrorCodes.VALIDATION_FAILED, ErrorMessages.VALIDATION_FAILED);
    }
  }

  // 人数变动后联动开放状态：达到核定容量自动满员，人数回落恢复开放。
  private void applyPopulation(Shelter shelter, int next) {
    shelter.currentPopulation = next;
    if (next >= shelter.capacity && ShelterStatus.OPEN.name().equals(shelter.openStatus)) {
      shelter.openStatus = ShelterStatus.FULL.name();
    } else if (next < shelter.capacity && ShelterStatus.FULL.name().equals(shelter.openStatus)) {
      shelter.openStatus = ShelterStatus.OPEN.name();
    }
  }

  private ShelterPlacementRecord newRecord(Shelter shelter, ShelterPlacementType type, int amount, String operator,
      int before, Long counterpartShelterId, String remark) {
    return new ShelterPlacementRecord(null, shelter.id, type.name(), amount,
        operator == null ? "" : operator.trim(), before, shelter.currentPopulation,
        counterpartShelterId, remark == null ? "" : remark.trim(), Instant.now().toString());
  }
}
