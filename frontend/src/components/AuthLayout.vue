<script setup>
import MonogramTile from './MonogramTile.vue';

// 登录/注册页左侧用来「展示商品」的示例，直接用真实品类和价格，
// 让人一眼看懂这个站是干什么的。
const samples = [
  { tile: '高等', name: '高等数学 第七版 上册', price: '12.50', color: '#2f6f8f' },
  { tile: '台灯', name: '宿舍护眼台灯', price: '35.00', color: '#7a5ca8' },
  { tile: '山地', name: '捷安特山地车 九成新', price: '260.00', color: '#1f7a6a' }
];
</script>

<template>
  <div class="auth">
    <aside class="auth__panel">
      <RouterLink class="auth__brand" :to="{ name: 'market' }">
        <span class="auth__mark" aria-hidden="true">集</span>
        <span class="auth__name">二手集市</span>
      </RouterLink>

      <p class="auth__lead">把宿舍里闲置的东西，<br />交给下一个需要它的人。</p>

      <ul class="auth__samples">
        <li v-for="(item, index) in samples" :key="item.name" :style="{ '--i': index }">
          <MonogramTile size="sm" :text="item.tile" :color="item.color" />
          <span class="auth__sname">{{ item.name }}</span>
          <span class="auth__sprice num">¥{{ item.price }}</span>
        </li>
      </ul>

      <p class="auth__foot">同校交易 · 当面自提 · 下单后凭订单联系卖家</p>
    </aside>

    <section class="auth__main">
      <div class="auth__box">
        <slot />
      </div>
    </section>
  </div>
</template>

<style scoped>
.auth {
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  min-height: 100vh;
}

/* ---------- 左：黑板 ---------- */
.auth__panel {
  display: flex;
  flex-direction: column;
  gap: 26px;
  padding: 48px 56px;
  background: var(--board);
  color: var(--chalk);
}

.auth__brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.auth__mark {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  font-size: 18px;
  font-weight: 800;
  color: var(--board-deep);
  background: var(--highlight);
  border-radius: var(--r-sm);
}

.auth__name {
  font-size: 17px;
  font-weight: 800;
  letter-spacing: 0.03em;
  color: #fff;
}

.auth__lead {
  margin-top: 8px;
  font-size: clamp(22px, 2.6vw, 30px);
  font-weight: 700;
  line-height: 1.5;
  letter-spacing: 0.01em;
  color: #fff;
}

.auth__samples {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 400px;
}

.auth__samples li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.13);
  border-radius: var(--r-sm);
  /* 整页唯一一次编排好的入场：三张商品卡依次落下 */
  animation: drop-in 0.5s cubic-bezier(0.2, 0.7, 0.3, 1) backwards;
  animation-delay: calc(var(--i) * 110ms + 120ms);
}

.auth__sname {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  color: var(--chalk);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.auth__sprice {
  font-size: 15px;
  font-weight: 800;
  color: var(--highlight);
}

.auth__foot {
  margin-top: auto;
  font-size: 12.5px;
  color: var(--chalk-soft);
}

@keyframes drop-in {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
}

/* ---------- 右：表单 ---------- */
.auth__main {
  display: grid;
  place-items: center;
  padding: 48px 32px;
  background: var(--paper);
}

.auth__box {
  width: 100%;
  max-width: 380px;
}

@media (max-width: 900px) {
  .auth {
    grid-template-columns: 1fr;
    min-height: 0;
  }

  .auth__panel {
    gap: 18px;
    padding: 28px 24px 32px;
  }

  .auth__lead {
    font-size: 20px;
  }

  .auth__samples {
    display: none;
  }

  .auth__foot {
    margin-top: 0;
  }

  .auth__main {
    padding: 32px 24px 56px;
  }
}
</style>
