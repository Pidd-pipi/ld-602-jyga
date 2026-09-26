<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{ current: number; capacity: number }>();
const percent = computed(() => (props.capacity > 0 ? Math.min(100, Math.round((props.current / props.capacity) * 100)) : 0));
</script>

<template>
  <div class="meter" role="progressbar" :aria-valuenow="current" aria-valuemin="0" :aria-valuemax="capacity" :title="`${current}/${capacity}`">
    <div class="meter-fill" :class="{ full: percent >= 100 }" :style="{ width: percent + '%' }"></div>
  </div>
</template>
