<script setup>
import { computed, ref, watch } from 'vue';
import MonogramTile from './MonogramTile.vue';
import { categoryColor, monogram } from '@/utils/format';

const props = defineProps({ goods: { type: Object, required: true }, size: { type: String, default: '' } });
const failed = ref(false);
const source = computed(() => {
  const url = (props.goods.imageUrl || '').trim();
  return /^(https?:\/\/|\/(?!\/))/.test(url) ? url : '';
});
watch(source, () => { failed.value = false; });
</script>

<template>
  <img v-if="source && !failed" :src="source" :alt="goods.name || '商品图片'"
    class="goods-image" :class="{ 'goods-image--large': size === 'lg' }"
    :loading="size === 'lg' ? 'eager' : 'lazy'" @error="failed = true" />
  <MonogramTile v-else :size="size" :text="monogram(goods.name)"
    :color="categoryColor(goods.category)" :label="goods.name" />
</template>

<style scoped>
.goods-image { width: 84px; height: 100px; flex-shrink: 0; object-fit: cover; border-radius: var(--r-sm); background: var(--paper); }
.goods-image--large { width: 100%; height: auto; max-height: 440px; aspect-ratio: 4 / 3; object-fit: contain; }
</style>
