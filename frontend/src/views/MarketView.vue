<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { goodsApi } from '@/api';
import { useGoodsStore } from '@/stores/goods';
import GoodsCard from '@/components/GoodsCard.vue';
import EmptyState from '@/components/EmptyState.vue';

const route = useRoute();
const router = useRouter();
const goods = useGoodsStore();

const keyword = ref(typeof route.query.q === 'string' ? route.query.q : '');
const category = ref(typeof route.query.c === 'string' ? route.query.c : '');
const results = ref(null);
const searching = ref(false);
const failed = ref(false);

/** results 为 null 时展示全部在架商品，否则展示搜索结果 */
const base = computed(() => results.value ?? goods.others);
const isSearching = computed(() => results.value !== null);

const categories = computed(() => {
  const set = new Set();
  base.value.forEach((item) => {
    const value = (item.category || '').trim();
    if (value) set.add(value);
  });
  return [...set].sort((a, b) => a.localeCompare(b, 'zh-Hans-CN'));
});

const visible = computed(() =>
  category.value
    ? base.value.filter((item) => (item.category || '').trim() === category.value)
    : base.value
);

function syncQuery() {
  const query = {};
  if (keyword.value.trim()) query.q = keyword.value.trim();
  if (category.value) query.c = category.value;
  router.replace({ name: 'market', query });
}

async function load() {
  failed.value = false;
  try {
    await goods.loadAll();
  } catch {
    failed.value = true;
  }
}

async function runSearch() {
  const value = keyword.value.trim();
  category.value = '';

  if (!value) {
    results.value = null;
    syncQuery();
    return;
  }

  searching.value = true;
  try {
    const data = await goodsApi.search(value);
    results.value = Array.isArray(data) ? data : [];
  } catch (error) {
    // 后端在没有匹配时返回 code 400「没有该商品」，这就是空结果，不是故障
    if (error?.code === 400) results.value = [];
    else failed.value = true;
  } finally {
    searching.value = false;
    syncQuery();
  }
}

function clearSearch() {
  keyword.value = '';
  results.value = null;
  category.value = '';
  syncQuery();
}

function pickCategory(value) {
  category.value = category.value === value ? '' : value;
  syncQuery();
}

onMounted(() => {
  load();
  if (keyword.value.trim()) runSearch();
});
</script>

<template>
  <div class="shell">
    <header class="board">
      <div class="board__text">
        <h1 class="board__title">集市</h1>
        <p class="board__lead">
          校园里正在转手的闲置，共
          <b class="num">{{ base.length }}</b>
          件<template v-if="isSearching">，匹配「{{ keyword }}」</template>
        </p>
      </div>

      <form class="search" role="search" @submit.prevent="runSearch">
        <input
          v-model="keyword"
          class="search__input"
          type="search"
          placeholder="搜书名、文具、日用品…"
          aria-label="搜索商品名称"
        />
        <button class="search__submit" type="submit" :disabled="searching">
          {{ searching ? '搜索中' : '搜索' }}
        </button>
        <button v-if="isSearching" class="search__clear" type="button" @click="clearSearch">
          清除
        </button>
      </form>
    </header>

    <div v-if="categories.length > 1" class="chips" role="group" aria-label="按分类筛选">
      <button
        class="chip"
        :class="{ 'is-on': !category }"
        type="button"
        @click="pickCategory('')"
      >
        全部
      </button>
      <button
        v-for="item in categories"
        :key="item"
        class="chip"
        :class="{ 'is-on': category === item }"
        type="button"
        @click="pickCategory(item)"
      >
        {{ item }}
      </button>
    </div>

    <div v-if="failed" class="fail">
      <p class="fail__title">没能拿到商品列表</p>
      <p class="fail__hint">确认后端已在 8080 端口启动，然后重试。</p>
      <el-button type="primary" @click="load">重新加载</el-button>
    </div>

    <div v-else-if="goods.loading && !base.length" class="grid" aria-busy="true">
      <div v-for="n in 6" :key="n" class="skeleton" aria-hidden="true"></div>
    </div>

    <template v-else>
      <div v-if="visible.length" class="grid">
        <GoodsCard v-for="item in visible" :key="item.id" :goods="item" />
      </div>

      <EmptyState
        v-else-if="isSearching"
        title="没有搜到这件东西"
        hint="换个说法试试，比如只搜「数学」或者「台灯」；也可以回集市看看全部在架商品。"
        action-text="看全部在架商品"
        :action-to="{ name: 'market' }"
      />
      <EmptyState
        v-else-if="category"
        title="这个分类下暂时没有商品"
        hint="换个分类看看，或者自己发布一件。"
        action-text="发布闲置"
        :action-to="{ name: 'sell' }"
      />
      <EmptyState
        v-else
        title="集市上还没有人摆摊"
        hint="你是第一个。把宿舍里用不上的书、文具或者日用品挂上来吧。"
        action-text="发布第一件闲置"
        :action-to="{ name: 'sell' }"
      />
    </template>
  </div>
</template>

<style scoped>
/* ---------- 页头：标题 + 统计句 + 搜索 ---------- */
.board {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 18px 28px;
  padding-bottom: 18px;
  border-bottom: 2px solid var(--board);
}

.board__text {
  flex: 1;
  min-width: 240px;
}

.board__title {
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.1;
}

.board__lead {
  margin-top: 7px;
  font-size: 14px;
  color: var(--ink-soft);
}

.board__lead b {
  font-size: 16px;
  color: var(--marker);
}

.search {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.search__input {
  width: 280px;
  padding: 10px 13px;
  font-family: inherit;
  font-size: 14px;
  color: var(--ink);
  background: var(--card);
  border: 1px solid var(--line-strong);
  border-radius: var(--r-sm);
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.search__input::placeholder {
  color: #93a49b;
}

.search__input:focus {
  outline: none;
  border-color: var(--board);
  box-shadow: 0 0 0 3px rgba(20, 69, 58, 0.12);
}

.search__submit {
  padding: 10px 18px;
  font-family: inherit;
  font-size: 14px;
  font-weight: 700;
  color: var(--card);
  background: var(--board);
  border: 1px solid var(--board);
  border-radius: var(--r-sm);
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.search__submit:hover:not(:disabled) {
  background: var(--board-deep);
}

.search__submit:disabled {
  opacity: 0.6;
  cursor: default;
}

.search__clear {
  padding: 10px 4px;
  font-family: inherit;
  font-size: 13px;
  color: var(--ink-soft);
  background: none;
  border: 0;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.search__clear:hover {
  color: var(--marker);
}

/* ---------- 分类 ---------- */
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 18px 0 20px;
}

.chip {
  padding: 5px 13px;
  font-family: inherit;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-soft);
  background: transparent;
  border: 1px solid var(--line-strong);
  border-radius: var(--r-sm);
  cursor: pointer;
  transition: color 0.15s ease, background-color 0.15s ease, border-color 0.15s ease;
}

.chip:hover {
  color: var(--ink);
  border-color: var(--ink-soft);
}

.chip.is-on {
  color: var(--card);
  background: var(--board);
  border-color: var(--board);
}

/* ---------- 商品网格 ---------- */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(310px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.chips + .grid,
.chips + .fail {
  margin-top: 0;
}

.skeleton {
  height: 120px;
  background: linear-gradient(100deg, #eceee8 30%, #f6f7f3 50%, #eceee8 70%);
  background-size: 220% 100%;
  border: 1px solid var(--line);
  border-radius: var(--r-sm);
  animation: sweep 1.2s ease-in-out infinite;
}

@keyframes sweep {
  from {
    background-position: 140% 0;
  }
  to {
    background-position: -40% 0;
  }
}

.fail {
  margin-top: 20px;
  padding: 28px 24px;
  background: var(--marker-soft);
  border: 1px solid var(--marker);
  border-radius: var(--r-sm);
}

.fail__title {
  font-size: 16px;
  font-weight: 700;
  color: var(--marker);
}

.fail__hint {
  margin: 6px 0 16px;
  font-size: 14px;
  color: var(--ink-soft);
}

@media (max-width: 720px) {
  .board {
    align-items: flex-start;
  }

  .search {
    width: 100%;
  }

  .search__input {
    flex: 1;
    width: auto;
    min-width: 0;
  }

  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
