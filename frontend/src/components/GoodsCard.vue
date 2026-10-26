<script setup>
import { computed } from 'vue';
import { RouterLink } from 'vue-router';
import GoodsImage from './GoodsImage.vue';
import { categoryColor, moneyDigits } from '@/utils/format';

const props = defineProps({
  goods: { type: Object, required: true },
  /** 整卡可点：列表页用；“我的在售”里因为要塞按钮，改成只有标题可点 */
  clickable: { type: Boolean, default: true },
  to: { type: [String, Object], default: null }
});

const color = computed(() => categoryColor(props.goods.category));

const soldOut = computed(() => Number(props.goods.stock ?? 0) <= 0);
const digits = computed(() => moneyDigits(props.goods.price));
const target = computed(
  () => props.to ?? { name: 'goods-detail', params: { id: props.goods.id } }
);
const tag = computed(() => (props.clickable ? RouterLink : 'article'));
</script>

<template>
  <component
    :is="tag"
    class="tag"
    :class="{ 'is-soldout': soldOut }"
    :style="{ '--cat': color }"
    v-bind="clickable ? { to: target } : {}"
  >
    <GoodsImage :goods="goods" />

    <div class="tag__body">
      <h3 class="tag__name">
        <RouterLink v-if="!clickable" :to="target">{{ goods.name || '未命名商品' }}</RouterLink>
        <template v-else>{{ goods.name || '未命名商品' }}</template>
      </h3>

      <p class="tag__meta">
        <span class="tag__cat">{{ goods.category || '未分类' }}</span>
        <i class="tag__rule" aria-hidden="true"></i>
        <span class="num">库存 {{ goods.stock ?? 0 }}</span>
      </p>

      <p class="tag__foot">
        <span class="tag__price num"><span class="tag__yuan">¥</span>{{ digits }}</span>
        <span v-if="soldOut" class="tag__sold">已售罄</span>
      </p>

      <slot name="actions" />
    </div>
  </component>
</template>

<style scoped>
.tag {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  padding: 16px;
  background: var(--card);
  border: 1px solid var(--line);
  border-left: 4px solid var(--cat);
  border-radius: var(--r-sm);
  box-shadow: var(--shadow-tag);
  transition: transform 0.16s ease, box-shadow 0.16s ease, border-color 0.16s ease;
}

a.tag:hover {
  transform: translate(-2px, -2px);
  box-shadow: var(--shadow-tag-lift);
}

a.tag:active {
  transform: translate(0, 0);
  box-shadow: 2px 2px 0 rgba(20, 69, 58, 0.16);
}

.tag.is-soldout {
  background: #fafbf8;
}

.tag.is-soldout .tag__name,
.tag.is-soldout .tile {
  opacity: 0.62;
}

.tag__body {
  min-width: 0;
  flex: 1;
}

.tag__name {
  font-size: 16px;
  font-weight: 700;
  line-height: 1.45;
  letter-spacing: -0.005em;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tag__meta {
  display: flex;
  align-items: center;
  gap: 9px;
  margin-top: 7px;
  font-size: 12.5px;
  color: var(--ink-soft);
}

.tag__cat {
  font-weight: 700;
  color: var(--cat);
}

.tag__rule {
  width: 1px;
  height: 11px;
  background: var(--line-strong);
}

.tag__foot {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 11px;
}

.tag__price {
  font-size: 22px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -0.02em;
  color: var(--marker);
}

.tag__yuan {
  font-size: 14px;
  font-weight: 700;
  margin-right: 1px;
}

.tag__sold {
  padding: 1px 6px;
  font-size: 11px;
  font-weight: 700;
  color: var(--marker);
  background: var(--marker-soft);
  border: 1px solid var(--marker);
  border-radius: var(--r-sm);
}
</style>
