export const mockData = {
  "warehouse": [
    {
      "id": 1,
      "name": "name 1",
      "district": "district 1",
      "address": "address 1",
      "manager_id": 1,
      "capacity_level": 92,
      "contact_phone": "13800000001",
      "status": "SUBMITTED"
    },
    {
      "id": 2,
      "name": "name 2",
      "district": "district 2",
      "address": "address 2",
      "manager_id": 2,
      "capacity_level": 104,
      "contact_phone": "13800000002",
      "status": "APPROVED"
    },
    {
      "id": 3,
      "name": "name 3",
      "district": "district 3",
      "address": "address 3",
      "manager_id": 3,
      "capacity_level": 116,
      "contact_phone": "13800000003",
      "status": "DRAFT"
    }
  ],
  "supplyItem": [
    {
      "id": 1,
      "sku_code": "sku code 1",
      "name": "name 1",
      "category": "WATER",
      "unit": "unit 1",
      "safety_stock": "safety stock 1",
      "expire_days": "expire days 1",
      "storage_requirement": "storage requirement 1"
    },
    {
      "id": 2,
      "sku_code": "sku code 2",
      "name": "name 2",
      "category": "MEDICAL",
      "unit": "unit 2",
      "safety_stock": "safety stock 2",
      "expire_days": "expire days 2",
      "storage_requirement": "storage requirement 2"
    },
    {
      "id": 3,
      "sku_code": "sku code 3",
      "name": "name 3",
      "category": "SHELTER",
      "unit": "unit 3",
      "safety_stock": "safety stock 3",
      "expire_days": "expire days 3",
      "storage_requirement": "storage requirement 3"
    }
  ],
  "inventoryBatch": [
    {
      "id": 1,
      "warehouse_id": 1,
      "supply_item_id": 1,
      "batch_no": "batch no 1",
      "quantity": 92,
      "expire_at": "2026-06-11T09:00:00Z",
      "inbound_source": "inbound source 1",
      "quality_status": "SUBMITTED"
    },
    {
      "id": 2,
      "warehouse_id": 2,
      "supply_item_id": 2,
      "batch_no": "batch no 2",
      "quantity": 104,
      "expire_at": "2026-06-12T09:00:00Z",
      "inbound_source": "inbound source 2",
      "quality_status": "APPROVED"
    },
    {
      "id": 3,
      "warehouse_id": 3,
      "supply_item_id": 3,
      "batch_no": "batch no 3",
      "quantity": 116,
      "expire_at": "2026-06-13T09:00:00Z",
      "inbound_source": "inbound source 3",
      "quality_status": "DRAFT"
    }
  ],
  "shelter": [
    {
      "id": 1,
      "name": "城东体育馆安置点",
      "district": "城东区",
      "capacity": 200,
      "current_population": 168,
      "remaining_slots": 32,
      "contact_person": "王建国",
      "risk_level": "LOW",
      "open_status": "OPEN",
      "recent_records": [
        {
          "id": 2,
          "shelter_id": 1,
          "record_type": "RECEIVE",
          "count": 68,
          "operator": "刘敏",
          "related_shelter_id": null,
          "resulting_population": 168,
          "created_at": "2026-09-25T09:30:00Z"
        },
        {
          "id": 1,
          "shelter_id": 1,
          "record_type": "RECEIVE",
          "count": 100,
          "operator": "刘敏",
          "related_shelter_id": null,
          "resulting_population": 100,
          "created_at": "2026-09-24T08:00:00Z"
        }
      ]
    },
    {
      "id": 2,
      "name": "滨河学校安置点",
      "district": "滨河区",
      "capacity": 120,
      "current_population": 120,
      "remaining_slots": 0,
      "contact_person": "李秀兰",
      "risk_level": "MEDIUM",
      "open_status": "FULL",
      "recent_records": [
        {
          "id": 3,
          "shelter_id": 2,
          "record_type": "RECEIVE",
          "count": 120,
          "operator": "陈刚",
          "related_shelter_id": null,
          "resulting_population": 120,
          "created_at": "2026-09-24T10:00:00Z"
        }
      ]
    },
    {
      "id": 3,
      "name": "西山社区安置点",
      "district": "西山区",
      "capacity": 80,
      "current_population": 0,
      "remaining_slots": 80,
      "contact_person": "赵铁柱",
      "risk_level": "HIGH",
      "open_status": "STANDBY",
      "recent_records": []
    }
  ],
  "dispatchOrder": [
    {
      "id": 1,
      "event_id": 1,
      "source_warehouse_id": 1,
      "shelter_id": 1,
      "priority": "priority 1",
      "status": "SUBMITTED",
      "requested_by": "requested by 1",
      "approved_by": "approved by 1",
      "dispatched_at": "2026-06-11T09:00:00Z"
    },
    {
      "id": 2,
      "event_id": 2,
      "source_warehouse_id": 2,
      "shelter_id": 2,
      "priority": "priority 2",
      "status": "APPROVED",
      "requested_by": "requested by 2",
      "approved_by": "approved by 2",
      "dispatched_at": "2026-06-12T09:00:00Z"
    },
    {
      "id": 3,
      "event_id": 3,
      "source_warehouse_id": 3,
      "shelter_id": 3,
      "priority": "priority 3",
      "status": "DRAFT",
      "requested_by": "requested by 3",
      "approved_by": "approved by 3",
      "dispatched_at": "2026-06-13T09:00:00Z"
    }
  ]
} as const;
