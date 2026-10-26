<script setup>
import { computed } from 'vue';
import { orderStatus } from '@/utils/format';

const props = defineProps({
  status: { type: Number, default: 0 }
});

const meta = computed(() => orderStatus(props.status));
</script>

<template>
  <span class="status" :class="'status--' + meta.tone">{{ meta.label }}</span>
</template>

<style scoped>
.status {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: 700;
  border-radius: var(--r-sm);
  border: 1px solid currentColor;
  white-space: nowrap;
}

.status--wait {
  color: #8a6300;
  background: var(--highlight-soft);
}

.status--transit {
  color: #2f6f8f;
  background: #e8f1f6;
}

.status--done {
  color: #2f7a4f;
  background: #e6f2ea;
}
</style>
