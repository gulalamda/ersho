<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ordersApi } from '@/api';
import { useGoodsStore } from '@/stores/goods';
import StatusTag from '@/components/StatusTag.vue';
import EmptyState from '@/components/EmptyState.vue';
import { formatDateTime, money } from '@/utils/format';

const goods = useGoodsStore();

const tab = ref('bought');
const bought = ref([]);
const sold = ref([]);
const loading = ref(false);
const failed = ref(false);
const acting = ref(null);

const list = computed(() => (tab.value === 'bought' ? bought.value : sold.value));
const pending = computed(
  () => list.value.filter((order) => Number(order.status) !== 2).length
);

function titleOf(order) {
  return goods.nameOf(order.goodsId) || `商品 #${order.goodsId}`;
}

function unitPrice(order) {
  const num = Number(order.num) || 1;
  return Number(order.price) / num;
}

async function load() {
  loading.value = true;
  failed.value = false;
  try {
    const [boughtList, soldList] = await Promise.all([ordersApi.bought(), ordersApi.sold()]);
    bought.value = Array.isArray(boughtList) ? boughtList : [];
    sold.value = Array.isArray(soldList) ? soldList : [];
    // 订单里只有 goodsId，靠商品索引把名字还原出来
    await goods.ensureLoaded().catch(() => {});
  } catch {
    failed.value = true;
  } finally {
    loading.value = false;
  }
}

async function setStatus(order, status) {
  const name = titleOf(order);
  const shipping = status === 1;

  try {
    await ElMessageBox.confirm(
      shipping
        ? `确认已经把「${name}」交给买家了吗？标记后买家就能确认收货。`
        : `确认已经拿到「${name}」了吗？确认后这笔交易就结束了，不能撤销。`,
      shipping ? '确认发货' : '确认收货',
      {
        confirmButtonText: shipping ? '确认发货' : '确认收货',
        cancelButtonText: '取消',
        type: 'info'
      }
    );
  } catch {
    return;
  }

  acting.value = order.id;
  try {
    await ordersApi.setStatus(order.id, status);
    ElMessage.success(shipping ? '已标记为已发货' : '交易完成');
    await load();
  } catch (error) {
    ElMessage.error(error.message || '操作失败，请重试');
  } finally {
    acting.value = null;
  }
}

onMounted(load);
</script>

<template>
  <div class="shell">
    <header class="head">
      <div>
        <h1 class="head__title">我的订单</h1>
        <p class="head__lead">
          当前列表里还有 <b class="num">{{ pending }}</b> 笔没走完：
          买家确认收货、卖家确认发货，交易才算结束。
        </p>
      </div>
    </header>

    <div class="tabs" role="tablist">
      <button
        class="tab"
        :class="{ 'is-on': tab === 'bought' }"
        type="button"
        role="tab"
        :aria-selected="tab === 'bought'"
        @click="tab = 'bought'"
      >
        我买到的
        <span class="tab__count num">{{ bought.length }}</span>
      </button>
      <button
        class="tab"
        :class="{ 'is-on': tab === 'sold' }"
        type="button"
        role="tab"
        :aria-selected="tab === 'sold'"
        @click="tab = 'sold'"
      >
        我卖出的
        <span class="tab__count num">{{ sold.length }}</span>
      </button>
    </div>

    <div v-if="failed" class="fail">
      <p class="fail__title">没能拿到订单</p>
      <p class="fail__hint">确认后端已在 8080 端口启动，然后重试。</p>
      <el-button type="primary" @click="load">重新加载</el-button>
    </div>

    <div v-else-if="loading && !list.length" class="rows" aria-busy="true">
      <div v-for="n in 3" :key="n" class="skeleton" aria-hidden="true"></div>
    </div>

    <div v-else-if="list.length" class="rows">
      <article v-for="order in list" :key="order.id" class="row">
        <div class="row__top">
          <h2 class="row__name">
            <RouterLink :to="{ name: 'goods-detail', params: { id: order.goodsId } }">
              {{ titleOf(order) }}
            </RouterLink>
          </h2>
          <StatusTag :status="Number(order.status)" />
        </div>

        <dl class="row__facts">
          <div>
            <dt>单价 × 数量</dt>
            <dd class="num">{{ money(unitPrice(order)) }} × {{ order.num ?? 1 }}</dd>
          </div>
          <div>
            <dt>合计</dt>
            <dd class="num row__total">{{ money(order.price) }}</dd>
          </div>
          <div>
            <dt>下单时间</dt>
            <dd class="num">{{ formatDateTime(order.createTime) }}</dd>
          </div>
          <div>
            <dt>收货地址</dt>
            <dd>{{ order.address || '未填写' }}</dd>
          </div>
        </dl>

        <div class="row__foot">
          <span class="row__no num">订单号 {{ order.orderNo || order.id }}</span>

          <button
            v-if="tab === 'sold' && Number(order.status) === 0"
            class="row__act"
            type="button"
            :disabled="acting === order.id"
            @click="setStatus(order, 1)"
          >
            确认发货
          </button>
          <button
            v-if="tab === 'bought' && Number(order.status) === 1"
            class="row__act"
            type="button"
            :disabled="acting === order.id"
            @click="setStatus(order, 2)"
          >
            确认收货
          </button>
        </div>
      </article>
    </div>

    <EmptyState
      v-else-if="tab === 'bought'"
      title="还没有买过东西"
      hint="集市上有人在出教材、台灯和自行车，看看有没有需要的。"
      action-text="去集市逛逛"
      :action-to="{ name: 'market' }"
    />
    <EmptyState
      v-else
      title="还没有卖出过东西"
      hint="把闲置挂上去，标价别太高，通常很快就有人问。"
      action-text="发布闲置"
      :action-to="{ name: 'sell' }"
    />
  </div>
</template>

<style scoped>
.head {
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

.tabs {
  display: flex;
  gap: 6px;
  margin: 20px 0 4px;
}

.tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 15px;
  font-family: inherit;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-soft);
  background: transparent;
  border: 1px solid var(--line-strong);
  border-radius: var(--r-sm);
  cursor: pointer;
  transition: color 0.15s ease, background-color 0.15s ease, border-color 0.15s ease;
}

.tab:hover {
  color: var(--ink);
  border-color: var(--ink-soft);
}

.tab.is-on {
  color: var(--card);
  background: var(--board);
  border-color: var(--board);
}

.tab__count {
  font-size: 12px;
  font-weight: 700;
  padding: 0 6px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.08);
}

.tab.is-on .tab__count {
  background: rgba(255, 255, 255, 0.22);
}

.rows {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 18px;
}

.row {
  padding: 18px 20px;
  background: var(--card);
  border: 1px solid var(--line);
  border-left: 4px solid var(--board);
  border-radius: var(--r-sm);
  box-shadow: var(--shadow-tag);
}

.row__top {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.row__name {
  flex: 1;
  min-width: 0;
  font-size: 16.5px;
  font-weight: 700;
  line-height: 1.45;
  word-break: break-word;
}

.row__name a {
  border-bottom: 2px solid transparent;
}

.row__name a:hover {
  border-bottom-color: var(--highlight);
}

.row__facts {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px 20px;
  margin: 16px 0 0;
  padding: 14px 0 0;
  border-top: 1px dashed var(--line-strong);
}

.row__facts div {
  min-width: 0;
}

.row__facts dt {
  font-size: 12px;
  color: var(--ink-soft);
}

.row__facts dd {
  margin: 3px 0 0;
  font-size: 14px;
  color: var(--ink);
  word-break: break-word;
}

.row__total {
  font-size: 17px !important;
  font-weight: 800;
  color: var(--marker) !important;
}

.row__foot {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 16px;
}

.row__no {
  font-size: 12px;
  color: var(--ink-soft);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row__act {
  margin-left: auto;
  flex-shrink: 0;
  padding: 7px 16px;
  font-family: inherit;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--board-deep);
  background: var(--highlight);
  border: 1px solid rgba(20, 69, 58, 0.25);
  border-radius: var(--r-sm);
  cursor: pointer;
  box-shadow: 2px 2px 0 rgba(20, 69, 58, 0.22);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.row__act:hover:not(:disabled) {
  transform: translate(-1px, -1px);
  box-shadow: 3px 3px 0 rgba(20, 69, 58, 0.26);
}

.row__act:disabled {
  opacity: 0.6;
  cursor: default;
}

.skeleton {
  height: 190px;
  background: #eceee8;
  border: 1px solid var(--line);
  border-radius: var(--r-sm);
}

.fail {
  margin-top: 18px;
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
