<script setup>
defineProps({
  title: { type: String, default: '这里还空着' },
  hint: { type: String, default: '' },
  actionText: { type: String, default: '' },
  actionTo: { type: [String, Object], default: null }
});
</script>

<template>
  <div class="empty">
    <p class="empty__title">{{ title }}</p>
    <p v-if="hint" class="empty__hint">{{ hint }}</p>
    <RouterLink v-if="actionText && actionTo" class="empty__action" :to="actionTo">
      {{ actionText }}
    </RouterLink>
  </div>
</template>

<style scoped>
.empty {
  padding: 44px 24px;
  text-align: left;
  background: #fbfcf9;
  border: 1px dashed var(--line-strong);
  border-radius: var(--r-sm);
}

.empty__title {
  font-size: 16px;
  font-weight: 700;
}

.empty__hint {
  margin-top: 6px;
  max-width: 46ch;
  font-size: 14px;
  color: var(--ink-soft);
}

.empty__action {
  display: inline-block;
  margin-top: 16px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 700;
  color: var(--card);
  background: var(--board);
  border-radius: var(--r-sm);
  transition: background-color 0.15s ease;
}

.empty__action:hover {
  background: var(--board-deep);
}
</style>
