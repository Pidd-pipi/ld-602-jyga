<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import CapacityMeter from "../components/common/CapacityMeter.vue";
import EmptyState from "../components/common/EmptyState.vue";
import StatCard from "../components/common/StatCard.vue";
import StatusBadge from "../components/common/StatusBadge.vue";
import { ShelterPlacementTypeText } from "../constants/ShelterPlacementType";
import { createReceiveForm, createTransferForm } from "../constructors/ShelterConstructor";
import { useShelterStore } from "../stores/ShelterStore";
import type { Shelter } from "../types/Shelter";
import { formatDate, formatRisk } from "../utils/formatters";

const store = useShelterStore();

type Mode = "receive" | "out" | "transfer";
const MODE_TABS: { key: Mode; label: string }[] = [
  { key: "receive", label: "接收登记" },
  { key: "out", label: "转出登记" },
  { key: "transfer", label: "点间互转" }
];

const mode = ref<Mode>("receive");
const receiveForm = reactive(createReceiveForm());
const transferForm = reactive(createTransferForm());
const error = ref("");
const notice = ref("");
const selectedShelterId = ref<number | null>(null);

onMounted(async () => {
  await store.load();
  selectedShelterId.value = store.rows[0]?.id ?? null;
});

const selectedShelter = computed(() => store.rows.find((row) => row.id === selectedShelterId.value) ?? null);
const selectedRecords = computed(() =>
  selectedShelterId.value == null ? [] : store.recordsOf(selectedShelterId.value).slice(0, 8)
);

function shelterName(id: number | null) {
  if (id == null) return "—";
  return store.rows.find((row) => row.id === id)?.name ?? `#${id}`;
}

function latestText(shelter: Shelter) {
  const record = store.latestOf(shelter.id);
  if (!record) return "—";
  return `${ShelterPlacementTypeText[record.change_type]} ${record.amount}人 · ${record.operator}`;
}

function openForm(next: Mode, shelter: Shelter) {
  error.value = "";
  notice.value = "";
  mode.value = next;
  if (next === "transfer") {
    transferForm.from_shelter_id = shelter.id;
    transferForm.to_shelter_id = store.rows.find((row) => row.id !== shelter.id)?.id ?? 0;
  } else {
    receiveForm.shelter_id = shelter.id;
  }
}

async function run(action: () => Promise<void>, okText: string) {
  error.value = "";
  notice.value = "";
  try {
    await action();
    notice.value = okText;
  } catch (err) {
    error.value = err instanceof Error ? err.message : String(err);
  }
}

function submitPlacement() {
  const payload = { ...receiveForm };
  const isReceive = mode.value === "receive";
  return run(async () => {
    if (isReceive) await store.receive(payload);
    else await store.transferOut(payload);
    receiveForm.amount = 1;
    receiveForm.remark = "";
  }, isReceive ? "接收登记完成，人数与状态已更新" : "转出登记完成，人数与状态已更新");
}

function submitTransfer() {
  const payload = { ...transferForm };
  return run(async () => {
    await store.transfer(payload);
    transferForm.amount = 1;
    transferForm.remark = "";
  }, "点间转移完成，两边人数已同步更新");
}

function changeStatus(shelter: Shelter, open_status: Shelter["open_status"]) {
  const operator = receiveForm.operator.trim() || shelter.contact_person || "值班员";
  const label = open_status === "CLOSED" ? "关闭" : open_status === "STANDBY" ? "停用" : "开放";
  return run(
    () => store.changeStatus({ shelter_id: shelter.id, open_status, operator }),
    `已${label}：${shelter.name}`
  );
}
</script>

<template>
  <section class="metrics">
    <StatCard label="安置点总数" :value="store.rows.length" />
    <StatCard label="在册安置人数" :value="store.totalPopulation" />
    <StatCard label="剩余安置名额" :value="store.totalRemaining" />
    <StatCard label="满员点位" :value="store.fullCount" />
  </section>

  <section class="panel wide">
    <h2>避难点列表</h2>
    <table class="shelter-table">
      <thead>
        <tr>
          <th>名称 / 辖区</th>
          <th>状态</th>
          <th>核定容量</th>
          <th>现有人数</th>
          <th>剩余名额</th>
          <th>容量水位</th>
          <th>最近记录</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="shelter in store.rows"
          :key="shelter.id"
          :class="{ selected: shelter.id === selectedShelterId }"
          @click="selectedShelterId = shelter.id"
        >
          <td>
            <strong>{{ shelter.name }}</strong>
            <div class="muted">{{ shelter.district }} · 风险{{ formatRisk(shelter.risk_level) }} · 联系人 {{ shelter.contact_person }}</div>
          </td>
          <td><StatusBadge :value="shelter.open_status" /></td>
          <td>{{ shelter.capacity }}</td>
          <td>{{ shelter.current_population }}</td>
          <td>{{ store.remaining(shelter) }}</td>
          <td><CapacityMeter :current="shelter.current_population" :capacity="shelter.capacity" /></td>
          <td>{{ latestText(shelter) }}</td>
          <td>
            <div class="action-row" @click.stop>
              <button @click="openForm('receive', shelter)">接收</button>
              <button @click="openForm('out', shelter)">转出</button>
              <button @click="openForm('transfer', shelter)">互转</button>
              <button v-if="shelter.open_status === 'OPEN' || shelter.open_status === 'FULL'" @click="changeStatus(shelter, 'STANDBY')">停用</button>
              <button v-if="shelter.open_status === 'STANDBY' || shelter.open_status === 'CLOSED'" @click="changeStatus(shelter, 'OPEN')">开放</button>
              <button v-if="shelter.open_status !== 'CLOSED'" class="danger" @click="changeStatus(shelter, 'CLOSED')">关闭</button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </section>

  <section class="panel">
    <h2>安置登记</h2>
    <div class="mode-tabs">
      <button
        v-for="tab in MODE_TABS"
        :key="tab.key"
        :class="{ active: mode === tab.key }"
        @click="mode = tab.key; error = ''; notice = ''"
      >{{ tab.label }}</button>
    </div>
    <div v-if="error" class="error-banner">{{ error }}</div>
    <div v-if="notice" class="notice-banner">{{ notice }}</div>

    <form v-if="mode !== 'transfer'" @submit.prevent="submitPlacement">
      <div class="form-grid">
        <label>避难点
          <select v-model.number="receiveForm.shelter_id">
            <option v-for="shelter in store.rows" :key="shelter.id" :value="shelter.id">{{ shelter.name }}</option>
          </select>
        </label>
        <label>人数
          <input v-model.number="receiveForm.amount" type="number" min="1" step="1" required />
        </label>
        <label>经办人
          <input v-model.trim="receiveForm.operator" type="text" placeholder="登记经办人姓名" required />
        </label>
        <label>备注
          <input v-model.trim="receiveForm.remark" type="text" placeholder="选填" />
        </label>
      </div>
      <div class="form-actions">
        <button type="submit">{{ mode === "receive" ? "登记接收" : "登记转出" }}</button>
      </div>
    </form>

    <form v-else @submit.prevent="submitTransfer">
      <div class="form-grid">
        <label>转出点
          <select v-model.number="transferForm.from_shelter_id">
            <option v-for="shelter in store.rows" :key="shelter.id" :value="shelter.id">{{ shelter.name }}（现有 {{ shelter.current_population }}）</option>
          </select>
        </label>
        <label>接收点
          <select v-model.number="transferForm.to_shelter_id">
            <option v-for="shelter in store.rows" :key="shelter.id" :value="shelter.id">{{ shelter.name }}（剩余 {{ store.remaining(shelter) }}）</option>
          </select>
        </label>
        <label>人数
          <input v-model.number="transferForm.amount" type="number" min="1" step="1" required />
        </label>
        <label>经办人
          <input v-model.trim="transferForm.operator" type="text" placeholder="登记经办人姓名" required />
        </label>
        <label>备注
          <input v-model.trim="transferForm.remark" type="text" placeholder="选填" />
        </label>
      </div>
      <div class="form-actions">
        <button type="submit">登记互转</button>
      </div>
    </form>
  </section>

  <section class="panel">
    <h2>安置记录{{ selectedShelter ? ` · ${selectedShelter.name}` : "" }}</h2>
    <EmptyState v-if="selectedRecords.length === 0" />
    <table v-else class="shelter-table">
      <thead>
        <tr>
          <th>时间</th>
          <th>类型</th>
          <th>人数</th>
          <th>经办人</th>
          <th>人数变动</th>
          <th>关联点位</th>
          <th>备注</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="record in selectedRecords" :key="record.id">
          <td>{{ formatDate(record.created_at) }}</td>
          <td><span class="record-type" :class="record.change_type">{{ ShelterPlacementTypeText[record.change_type] }}</span></td>
          <td>{{ record.amount }}</td>
          <td>{{ record.operator }}</td>
          <td>{{ record.before_population }} → {{ record.after_population }}</td>
          <td>{{ shelterName(record.counterpart_shelter_id) }}</td>
          <td>{{ record.remark || "—" }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
