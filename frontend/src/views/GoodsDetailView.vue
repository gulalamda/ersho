<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ordersApi } from '@/api';
import { useGoodsStore } from '@/stores/goods';
import { useUserStore } from '@/stores/user';
import { categoryColor, money } from '@/utils/format';
import GoodsImage from '@/components/GoodsImage.vue';
import EmptyState from '@/components/EmptyState.vue';

const props = defineProps({ id: { type: [String, Number], required: true } });

const router = useRouter();
const goods = useGoodsStore();
const user = useUserStore();

const loading = ref(true);
const submitting = ref(false);
const form = reactive({ num: 1, address: '' });

const item = computed(() => goods.byId(props.id));
const isMine = computed(
  () => item.value && user.profile && item.value.sellerId === user.profile.id
);
const soldOut = computed(() => Number(item.value?.stock ?? 0) <= 0);
const outOfRange = computed(() => Number(form.num) > Number(item.value?.stock ?? 0));
const total = computed(() => Number(item.value?.price ?? 0) * Number(form.num || 0));

async function load() {
  loading.value = true;
  try {
    await goods.ensureLoaded();
    await user.loadProfile().catch(() => {});
    form.num = 1;
    form.address = user.address || '';
  } finally {
    loading.value = false;
  }
}

async function submit() {
  if (!item.value) return;
  if (!form.num || form.num < 1) {
    ElMessage.warning('购买数量至少为 1');
    return;
  }
  if (outOfRange.value) {
    ElMessage.warning(`库存只有 ${item.value.stock} 件`);
    return;
  }

  submitting.value = true;
  try {
    await ordersApi.create({
      goodsId: item.value.id,
      num: Number(form.num),
      address: form.address.trim() || null
    });
    ElMessage.success('下单成功，去订单页看看');
    await goods.loadAll();
    router.push({ name: 'my-orders' });
  } catch (error) {
    ElMessage.error(error.message || '下单失败，请重试');
  } finally {
    submitting.value = false;
  }
}

onMounted(load);
watch(() => props.id, load);
</script>

<template>
  <div class="shell">
    <nav class="crumb" aria-label="面包屑">
      <RouterLink :to="{ name: 'market' }">集市</RouterLink>
      <span aria-hidden="true">/</span>
      <span>{{ item?.name || '商品详情' }}</span>
    </nav>

    <div v-if="loading && !item" class="loading" aria-busy="true">
      <div class="loading__cover"></div>
      <div class="loading__side"></div>
    </div>

    <EmptyState
      v-else-if="!item"
      title="这件东西不在了"
      hint="它可能已经被卖家下架，或者已经卖掉了。回集市看看别的吧。"
      action-text="回到集市"
      :action-to="{ name: 'market' }"
    />

    <div v-else class="detail">
      <div class="detail__main">
        <GoodsImage size="lg" :goods="item" />

        <h1 class="detail__name">{{ item.name }}</h1>

        <p class="detail__meta">
          <span class="detail__cat" :style="{ color: categoryColor(item.category) }">
            {{ item.category || '未分类' }}
          </span>
          <i class="detail__rule" aria-hidden="true"></i>
          <span class="num">库存 {{ item.stock ?? 0 }} 件</span>
          <i class="detail__rule" aria-hidden="true"></i>
          <span class="num">卖家编号 {{ item.sellerId }}</span>
        </p>

        <section class="block">
          <h2 class="block__title">卖家写的说明</h2>
          <p v-if="item.description" class="block__body">{{ item.description }}</p>
          <p v-else class="block__body block__body--quiet">卖家没有留下说明。</p>
        </section>

        <section class="block">
          <h2 class="block__title">交易提示</h2>
          <ul class="notes">
            <li>校内当面自提最省事，地址填宿舍楼栋就行。</li>
            <li>先下单再联系卖家，订单就是你们的凭证。</li>
            <li>收到东西再点「确认收货」，别提前点。</li>
          </ul>
        </section>
      </div>

      <aside class="buy">
        <p class="buy__label">价格</p>
        <p class="buy__price num">{{ money(item.price) }}</p>

        <template v-if="isMine">
          <p class="buy__mine">这是你发布的商品，不能购买自己的东西。</p>
          <RouterLink class="buy__submit" :to="{ name: 'sell-edit', params: { id: item.id } }">
            去编辑
          </RouterLink>
        </template>

        <template v-else-if="soldOut">
          <p class="buy__mine">已经卖光了，看看别家的吧。</p>
          <RouterLink class="buy__submit" :to="{ name: 'market' }">继续逛集市</RouterLink>
        </template>

        <template v-else>
          <div class="field">
            <label class="field__label" for="buy-num">购买数量</label>
            <el-input-number
              id="buy-num"
              v-model="form.num"
              :min="1"
              :max="Number(item.stock) || 1"
              controls-position="right"
              style="width: 100%"
            />
          </div>

          <div class="field">
            <label class="field__label" for="buy-address">收货地址 / 宿舍</label>
            <el-input
              id="buy-address"
              v-model="form.address"
              placeholder="例如：3 号楼 502"
              maxlength="60"
            />
          </div>

          <div class="buy__sum">
            <span>合计</span>
            <b class="num">{{ money(total) }}</b>
          </div>

          <el-button
            class="buy__submit"
            type="primary"
            size="large"
            :loading="submitting"
            @click="submit"
          >
            立即购买
          </el-button>
        </template>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.crumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--ink-soft);
}

.crumb a {
  font-weight: 600;
  color: var(--board);
  border-bottom: 1px solid var(--line-strong);
}

.crumb a:hover {
  border-bottom-color: var(--board);
}

.detail {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 36px;
  margin-top: 18px;
  align-items: start;
}

.detail__name {
  margin-top: 22px;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.35;
  letter-spacing: -0.01em;
  word-break: break-word;
}

.detail__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  font-size: 13.5px;
  color: var(--ink-soft);
}

.detail__cat {
  font-weight: 700;
}

.detail__rule {
  width: 1px;
  height: 12px;
  background: var(--line-strong);
}

.block {
  margin-top: 32px;
  padding-top: 22px;
  border-top: 1px solid var(--line);
}

.block__title {
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.block__body {
  margin-top: 10px;
  max-width: 68ch;
  font-size: 15px;
  line-height: 1.8;
  color: #2c433b;
  white-space: pre-wrap;
  word-break: break-word;
}

.block__body--quiet {
  color: var(--ink-soft);
}

.notes {
  margin: 10px 0 0;
  padding-left: 20px;
  max-width: 68ch;
  font-size: 14px;
  line-height: 1.9;
  color: var(--ink-soft);
}

/* ---------- 右侧购买栏 ---------- */
.buy {
  position: sticky;
  top: calc(var(--h-masthead) + 20px);
  padding: 22px;
  background: var(--card);
  border: 1px solid var(--line);
  border-top: 4px solid var(--marker);
  border-radius: var(--r-sm);
  box-shadow: var(--shadow-tag);
}

.buy__label {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: var(--ink-soft);
}

.buy__price {
  margin-top: 2px;
  font-size: 38px;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.03em;
  color: var(--marker);
}

.field {
  margin-top: 18px;
}

.field__label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-soft);
}

.buy__sum {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 20px;
  padding-top: 14px;
  border-top: 1px solid var(--line);
  font-size: 14px;
  color: var(--ink-soft);
}

.buy__sum b {
  font-size: 22px;
  font-weight: 800;
  color: var(--ink);
}

.buy__submit {
  display: block;
  width: 100%;
  margin-top: 16px;
  text-align: center;
}

a.buy__submit {
  padding: 12px 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--card);
  background: var(--board);
  border-radius: var(--r-sm);
}

a.buy__submit:hover {
  background: var(--board-deep);
}

.buy__mine {
  margin: 16px 0 0;
  padding: 12px 14px;
  font-size: 13.5px;
  line-height: 1.7;
  color: var(--ink-soft);
  background: var(--paper);
  border: 1px dashed var(--line-strong);
  border-radius: var(--r-sm);
}

.loading {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 36px;
  margin-top: 18px;
}

.loading__cover {
  height: 400px;
  background: #eceee8;
  border-radius: var(--r-md);
}

.loading__side {
  height: 260px;
  background: #eceee8;
  border-radius: var(--r-sm);
}

@media (max-width: 900px) {
  .detail,
  .loading {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .buy {
    position: static;
  }

  .detail__name {
    font-size: 23px;
  }

  .buy__price {
    font-size: 32px;
  }
}
</style>
