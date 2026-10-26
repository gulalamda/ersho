<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { goodsApi } from '@/api';
import { useGoodsStore } from '@/stores/goods';
import GoodsCard from '@/components/GoodsCard.vue';
import EmptyState from '@/components/EmptyState.vue';

const goods = useGoodsStore();
const loading = ref(false);
const failed = ref(false);

const list = computed(() => goods.mine);
const onSale = computed(() => list.value.filter((item) => Number(item.stock ?? 0) > 0).length);
const soldOut = computed(() => list.value.filter((item) => Number(item.stock ?? 0) <= 0).length);

async function load() {
  loading.value = true;
  failed.value = false;
  try {
    await goods.loadAll();
  } catch {
    failed.value = true;
  } finally {
    loading.value = false;
  }
}

async function takeDown(item) {
  try {
    await ElMessageBox.confirm(
      `下架后「${item.name}」会从集市上消失，买家就搜不到了。`,
      '下架这件商品',
      { confirmButtonText: '确认下架', cancelButtonText: '再想想', type: 'warning' }
    );
  } catch {
    return;
  }

  try {
    await goodsApi.remove(item.id);
    goods.removeMine(item.id);
    ElMessage.success('已下架');
  } catch (error) {
    ElMessage.error(error.message || '下架失败，请重试');
  }
}

onMounted(load);
</script>

<template>
  <div class="shell">
    <header class="head">
      <div>
        <h1 class="head__title">我的在售</h1>
        <p class="head__lead">
          在架 <b class="num">{{ onSale }}</b> 件，已售罄 <b class="num">{{ soldOut }}</b> 件。
          库存为 0 的商品会显示「已售罄」，买家下不了单。
        </p>
      </div>
      <RouterLink class="head__cta" :to="{ name: 'sell' }">发布闲置</RouterLink>
    </header>

    <div v-if="failed" class="fail">
      <p class="fail__title">没能拿到你的商品</p>
      <p class="fail__hint">确认后端已在 8080 端口启动，然后重试。</p>
      <el-button type="primary" @click="load">重新加载</el-button>
    </div>

    <div v-else-if="loading && !list.length" class="grid" aria-busy="true">
      <div v-for="n in 4" :key="n" class="skeleton" aria-hidden="true"></div>
    </div>

    <div v-else-if="list.length" class="grid">
      <GoodsCard v-for="item in list" :key="item.id" :goods="item" :clickable="false">
        <template #actions>
          <div class="ops">
            <RouterLink class="ops__edit" :to="{ name: 'sell-edit', params: { id: item.id } }">
              编辑
            </RouterLink>
            <button class="ops__down" type="button" @click="takeDown(item)">下架</button>
          </div>
        </template>
      </GoodsCard>
    </div>

    <EmptyState
      v-else
      title="你还没有发布过东西"
      hint="书架上看完不再翻的书、抽屉里多的文具、毕业带不走的台灯，都可以挂上来。"
      action-text="发布第一件闲置"
      :action-to="{ name: 'sell' }"
    />
  </div>
</template>

<style scoped>
.head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 16px 24px;
  padding-bottom: 18px;
  border-bottom: 2px solid var(--board);
}

.head__title {
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.1;
}

.head__lead {
  margin-top: 7px;
  font-size: 14px;
  color: var(--ink-soft);
}

.head__lead b {
  color: var(--marker);
}

.head__cta {
  margin-left: auto;
  padding: 9px 18px;
  font-size: 14px;
  font-weight: 700;
  color: var(--board-deep);
  background: var(--highlight);
  border-radius: var(--r-sm);
  box-shadow: 2px 2px 0 rgba(20, 69, 58, 0.3);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.head__cta:hover {
  transform: translate(-1px, -1px);
  box-shadow: 3px 3px 0 rgba(20, 69, 58, 0.34);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(310px, 1fr));
  gap: 16px;
  margin-top: 22px;
}

.ops {
  display: flex;
  gap: 8px;
  margin-top: 14px;
  padding-top: 13px;
  border-top: 1px dashed var(--line-strong);
}

.ops__edit,
.ops__down {
  padding: 5px 14px;
  font-family: inherit;
  font-size: 13px;
  font-weight: 600;
  border-radius: var(--r-sm);
  cursor: pointer;
  transition: background-color 0.15s ease, color 0.15s ease, border-color 0.15s ease;
}

.ops__edit {
  color: var(--board);
  background: transparent;
  border: 1px solid var(--line-strong);
}

.ops__edit:hover {
  border-color: var(--board);
  background: #eef2ee;
}

.ops__down {
  color: var(--marker);
  background: transparent;
  border: 1px solid var(--marker-soft);
}

.ops__down:hover {
  color: var(--card);
  background: var(--marker);
  border-color: var(--marker);
}

.skeleton {
  height: 168px;
  background: #eceee8;
  border: 1px solid var(--line);
  border-radius: var(--r-sm);
}

.fail {
  margin-top: 22px;
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
</style>
