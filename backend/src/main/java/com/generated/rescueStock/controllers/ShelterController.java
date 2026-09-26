package com.generated.rescueStock.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.generated.rescueStock.services.ShelterService;
import com.generated.rescueStock.services.ShelterServiceException;
import com.generated.rescueStock.types.ShelterPayload;

@RestController
@RequestMapping("/api/shelter")
public class ShelterController {
  private final ShelterService service;

  public ShelterController(ShelterService service) {
    this.service = service;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list();
  }

  @GetMapping("/records")
  public List<Map<String, Object>> records(@RequestParam(required = false) Long shelterId) {
    return service.records(shelterId);
  }

  @PostMapping("/receive")
  public ResponseEntity<?> receive(@RequestBody(required = false) ShelterPayload.Placement payload) {
    try {
      return ResponseEntity.ok(service.receive(payload));
    } catch (ShelterServiceException ex) {
      return reject(ex);
    }
  }

  @PostMapping("/transfer-out")
  public ResponseEntity<?> transferOut(@RequestBody(required = false) ShelterPayload.Placement payload) {
    try {
      return ResponseEntity.ok(service.transferOut(payload));
    } catch (ShelterServiceException ex) {
      return reject(ex);
    }
  }

  @PostMapping("/transfer")
  public ResponseEntity<?> transfer(@RequestBody(required = false) ShelterPayload.Transfer payload) {
    try {
      return ResponseEntity.ok(service.transfer(payload));
    } catch (ShelterServiceException ex) {
      return reject(ex);
    }
  }

  @PostMapping("/status")
  public ResponseEntity<?> changeStatus(@RequestBody(required = false) ShelterPayload.StatusChange payload) {
    try {
      return ResponseEntity.ok(service.changeStatus(payload));
    } catch (ShelterServiceException ex) {
      return reject(ex);
    }
  }

  private ResponseEntity<Map<String, Object>> reject(ShelterServiceException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("code", ex.code(), "message", ex.getMessage()));
  }
}
