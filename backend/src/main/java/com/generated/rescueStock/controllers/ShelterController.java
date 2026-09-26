package com.generated.rescueStock.controllers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.rescueStock.routes.ShelterRoutes;
import com.generated.rescueStock.services.ServiceException;
import com.generated.rescueStock.services.ShelterService;
import com.generated.rescueStock.types.PlacementPayload;

@RestController
@RequestMapping(ShelterRoutes.PATH)
public class ShelterController {
  private final ShelterService service;

  public ShelterController(ShelterService service) {
    this.service = service;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list();
  }

  @PostMapping("/{id}/receive")
  public ResponseEntity<Object> receive(@PathVariable Long id, @RequestBody PlacementPayload payload) {
    try {
      return ResponseEntity.ok(service.receive(id, payload.count(), payload.operator()));
    } catch (ServiceException error) {
      return badRequest(error);
    }
  }

  @PostMapping("/{id}/transfer-out")
  public ResponseEntity<Object> transferOut(@PathVariable Long id, @RequestBody PlacementPayload payload) {
    try {
      return ResponseEntity.ok(service.transferOut(id, payload.count(), payload.operator()));
    } catch (ServiceException error) {
      return badRequest(error);
    }
  }

  @PostMapping("/transfer")
  public ResponseEntity<Object> transfer(@RequestBody PlacementPayload payload) {
    try {
      return ResponseEntity.ok(service.transfer(payload.fromId(), payload.toId(), payload.count(), payload.operator()));
    } catch (ServiceException error) {
      return badRequest(error);
    }
  }

  @PostMapping("/{id}/status")
  public ResponseEntity<Object> updateStatus(@PathVariable Long id, @RequestBody PlacementPayload payload) {
    try {
      return ResponseEntity.ok(service.updateStatus(id, payload.status(), payload.operator()));
    } catch (ServiceException error) {
      return badRequest(error);
    }
  }

  private ResponseEntity<Object> badRequest(ServiceException error) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("code", error.getCode());
    body.put("message", error.getMessage());
    return ResponseEntity.badRequest().body(body);
  }
}
