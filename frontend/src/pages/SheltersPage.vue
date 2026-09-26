<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import CapacityMeter from "../components/common/CapacityMeter.vue";
import EmptyState from "../components/common/EmptyState.vue";
import StatCard from "../components/common/StatCard.vue";
import StatusBadge from "../components/common/StatusBadge.vue";
import { PlacementRecordTypeText } from "../constants/PlacementRecordType";
import { ShelterStatusText, type ShelterStatus } from "../constants/ShelterStatus";
import { useShelterStore } from "../stores/ShelterStore";
import type { PlacementRecord } from "../types/PlacementRecord";
import type { Shelter } from "../types/Shelter";
import { formatDate, formatRisk } from "../utils/formatters";

const store = useShelterStore();
onMounted(() => {
  void store.load();
});

const operator = ref("");
const submitting = ref(false);

const totalPopulation = computed(() => store.rows.reduce((sum, row) => sum + row.current_population, 0));
const totalRemaining = computed(() => store.rows.reduce((sum, row) => sum + row.remaining_slots, 0));

type ActionKind = "RECEIVE" | "TRANSFER_OUT" | "TRANSFER";
const ACTION_TEXT: Record<ActionKind, string> = {
  RECEIVE: "登记接收",
  TRANSFER_OUT: "登记转出",
  TRANSFER: "两点转移"
};

const form = reactive({
  kind: "RECEIVE" as ActionKind,
  shelter: null as Shelter | null,
  targetId: 0,
  count: 1
});

const transferTargets = computed(() =>
  store.rows.filter((row) => row.id !== form.shelter?.id && row.open_status === "OPEN" && row.remaining_slots > 0)
);

function openForm(kind: ActionKind, shelter: Shelter) {
  form.kind = kind;
  form.shelter = shelter;
  form.targetId = transferTargets.value[0]?.id ?? 0;
  form.count = 1;
}

function closeForm() {
  form.shelter = null;
}

async function submitForm() {
  if (!form.shelter || submitting.value) return;
  submitting.value = true;
  const operatorName = operator.value.trim();
  let ok = false;
  if (form.kind === "RECEIVE") {
    ok = await store.receive(form.shelter.id, Number(form.count), operatorName);
  } else if (form.kind === "TRANSFER_OUT") {
    ok = await store.transferOut(form.shelter.id, Number(form.count), operatorName);
  } else {
    ok = await store.transfer(form.shelter.id, Number(form.targetId), Number(form.count), operatorName);
  }
  submitting.value = false;
  if (ok) closeForm();
}

async function changeStatus(shelter: Shelter, status: ShelterStatus) {
  if (submitting.value) return;
  submitting.value = true;
  await store.changeStatus(shelter.id, status, operator.value.trim());
  submitting.value = false;
}

const selectedId = ref<number | null>(null);
const selected = computed(() => store.rows.find((row) => row.id === selectedId.value) ?? null);

function toggleRecords(shelter: Shelter) {
  selectedId.value = selectedId.value === shelter.id ? null : shelter.id;
}

function shelterName(id: number | null): string {
  if (id == null) return "";
  return store.rows.find((row) => row.id === id)?.name ?? `#${id}`;
}

function recordTarget(record: PlacementRecord): string {
  if (record.record_type === "TRANSFER_OUT") return `转至 ${shelterName(record.related_shelter_id)}`;
  if (record.record_type === "TRANSFER_IN") return `来自 ${shelterName(record.related_shelter_id)}`;
  return "";
}
</script>

<template>
  <section class="metrics">
    <StatCard label="避难点总数" :value="store.rows.length" />
    <StatCard label="现有安置人数" :value="totalPopulation" />
    <StatCard label="剩余名额" :value="totalRemaining" />
  </section>

  <div v-if="store.error" class="error-banner">{{ store.error }}</div>

  <section class="panel">
    <div class="toolbar">
      <label class="field inline">
        <span>经办人</span>
        <input v-model="operator" type="text" placeholder="登记接收/转出时必填" />
      </label>
      <button class="btn" type="button" @click="store.load()">刷新</button>
      <span v-if="store.loading" class="muted">加载中…</span>
    </div>

    <table v-if="store.rows.length" class="table">
      <thead>
        <tr>
          <th>名称</th>
          <th>状态</th>
          <th>现有人数</th>
          <th>核定容量</th>
          <th>剩余名额</th>
          <th>风险</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in store.rows" :key="row.id">
          <td>
            <strong>{{ row.name }}</strong>
            <div class="muted">{{ row.district }} · 联系人 {{ row.contact_person }}</div>
          </td>
          <td><StatusBadge :value="ShelterStatusText[row.open_status]" /></td>
          <td>
            <span class="num">{{ row.current_population }}</span>
            <CapacityMeter :current="row.current_population" :capacity="row.capacity" />
          </td>
          <td class="num">{{ row.capacity }}</td>
          <td class="num">{{ row.remaining_slots }}</td>
          <td>{{ formatRisk(row.risk_level) }}</td>
          <td>
            <div class="actions">
              <button class="btn primary" type="button" :disabled="row.open_status !== 'OPEN'" title="登记接收安置人员" @click="openForm('RECEIVE', row)">接收</button>
              <button class="btn" type="button" :disabled="row.current_population <= 0" title="登记人员转出" @click="openForm('TRANSFER_OUT', row)">转出</button>
              <button class="btn" type="button" :disabled="row.current_population <= 0" title="转移到其他避难点" @click="openForm('TRANSFER', row)">转移</button>
              <button class="btn" type="button" @click="toggleRecords(row)">记录</button>
              <template v-if="row.open_status === 'OPEN' || row.open_status === 'FULL'">
                <button class="btn" type="button" @click="changeStatus(row, 'STANDBY')">停用</button>
                <button class="btn danger" type="button" @click="changeStatus(row, 'CLOSED')">关闭</button>
              </template>
              <template v-else-if="row.open_status === 'STANDBY'">
                <button class="btn" type="button" @click="changeStatus(row, 'OPEN')">启用</button>
                <button class="btn danger" type="button" @click="changeStatus(row, 'CLOSED')">关闭</button>
              </template>
              <button v-else class="btn" type="button" @click="changeStatus(row, 'OPEN')">启用</button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
    <EmptyState v-else />
  </section>

  <section v-if="form.shelter" class="panel">
    <h2>{{ ACTION_TEXT[form.kind] }} — {{ form.shelter.name }}</h2>
    <div class="form-grid">
      <label class="field">
        <span>人数</span>
        <input v-model.number="form.count" type="number" min="1" />
      </label>
      <label v-if="form.kind === 'TRANSFER'" class="field">
        <span>目标避难点</span>
        <select v-model.number="form.targetId">
          <option v-for="target in transferTargets" :key="target.id" :value="target.id">
            {{ target.name }}（剩余 {{ target.remaining_slots }}）
          </option>
        </select>
      </label>
      <div class="actions">
        <button class="btn primary" type="button" :disabled="submitting || (form.kind === 'TRANSFER' && !form.targetId)" @click="submitForm">确认{{ ACTION_TEXT[form.kind] }}</button>
        <button class="btn" type="button" @click="closeForm">取消</button>
      </div>
    </div>
    <p class="muted">现有 {{ form.shelter.current_population }} 人 / 容量 {{ form.shelter.capacity }} 人，剩余 {{ form.shelter.remaining_slots }} 个名额。</p>
  </section>

  <section v-if="selected" class="panel">
    <h2>最近安置记录 — {{ selected.name }}</h2>
    <ul v-if="selected.recent_records.length" class="records">
      <li v-for="record in selected.recent_records" :key="record.id">
        <span>{{ formatDate(record.created_at) }}</span>
        <StatusBadge :value="PlacementRecordTypeText[record.record_type]" />
        <span class="num">{{ record.count }} 人</span>
        <span v-if="recordTarget(record)" class="muted">{{ recordTarget(record) }}</span>
        <span>经办人：{{ record.operator }}</span>
        <span class="muted">变动后 {{ record.resulting_population }} 人</span>
      </li>
    </ul>
    <EmptyState v-else />
  </section>
</template>
