<script setup>
import { computed } from 'vue';
import { hexToRgba } from '@/utils/color';

/**
 * 后端商品表没有图片字段，与其放一堆灰色占位图，
 * 不如用商品名的首字生成一块“字形封面”，颜色跟着分类走。
 */
const props = defineProps({
  text: { type: String, default: '闲' },
  color: { type: String, default: '#52685e' },
  size: { type: String, default: 'md' },
  label: { type: String, default: '' }
});

const style = computed(() => ({
  '--cat': props.color,
  '--cat-bg': hexToRgba(props.color, 0.09),
  '--cat-line': hexToRgba(props.color, 0.3)
}));
</script>

<template>
  <div
    class="tile"
    :class="'tile--' + size"
    :style="style"
    role="img"
    :aria-label="label ? label + ' 的封面' : text"
  >
    <span class="tile__glyph">{{ text }}</span>
  </div>
</template>

<style scoped>
.tile {
  position: relative;
  display: grid;
  place-items: center;
  background: var(--cat-bg);
  border: 1px solid var(--cat-line);
  border-radius: var(--r-sm);
  color: var(--cat);
  overflow: hidden;
  flex-shrink: 0;
}

.tile__glyph {
  position: relative;
  font-weight: 700;
  line-height: 1;
  letter-spacing: 0.04em;
}

/* 小尺寸：不加纹理，避免整片卡片网格变花 */
.tile--sm {
  width: 40px;
  height: 40px;
}

.tile--sm .tile__glyph {
  font-size: 15px;
}

.tile--md {
  width: 72px;
  height: 72px;
}

.tile--md .tile__glyph {
  font-size: 22px;
}

/* 详情页的大封面：横线格纸纹理，像一张商品卡 */
.tile--lg {
  width: 100%;
  min-height: 190px;
  border-radius: var(--r-md);
}

.tile--lg .tile__glyph {
  font-size: clamp(48px, 9vw, 76px);
}

.tile--lg::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: repeating-linear-gradient(
    to bottom,
    transparent 0 15px,
    var(--cat-line) 15px 16px
  );
  opacity: 0.3;
}
</style>
