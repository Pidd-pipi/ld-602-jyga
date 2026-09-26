<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{ current: number; capacity: number }>();

const percent = computed(() =>
  props.capacity > 0 ? Math.min(100, Math.round((props.current / props.capacity) * 100)) : 0
);
const remaining = computed(() => Math.max(0, props.capacity - props.current));
</script>

<template>
  <div class="capacity-meter">
    <div class="capacity-track">
      <div class="capacity-fill" :class="{ full: remaining === 0 }" :style="{ width: percent + '%' }" />
    </div>
    <span class="capacity-text">{{ current }}/{{ capacity }}（剩余 {{ remaining }}）</span>
  </div>
</template>
