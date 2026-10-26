<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const user = useUserStore();

const navs = [
  { name: 'market', label: '集市' },
  { name: 'my-goods', label: '我的在售' },
  { name: 'my-orders', label: '我的订单' }
];

const active = computed(() => {
  if (route.name === 'goods-detail') return 'market';
  if (route.name === 'sell' || route.name === 'sell-edit') return 'sell';
  return route.name;
});

async function onCommand(command) {
  if (command === 'profile') {
    router.push({ name: 'profile' });
    return;
  }
  if (command !== 'logout') return;
  try {
    await ElMessageBox.confirm('退出后需要重新登录才能继续买卖。', '退出登录', {
      confirmButtonText: '退出登录',
      cancelButtonText: '再想想',
      type: 'warning'
    });
  } catch {
    return;
  }
  user.logout();
  ElMessage.success('已退出登录');
  router.push({ name: 'login' });
}
</script>

<template>
  <div class="shell-root">
    <header class="masthead">
      <div class="shell masthead__inner">
        <RouterLink class="brand" :to="{ name: 'market' }">
          <span class="brand__mark" aria-hidden="true">集</span>
          <span class="brand__text">
            <strong>二手集市</strong>
            <em>校园闲置交易</em>
          </span>
        </RouterLink>

        <nav class="nav" aria-label="主导航">
          <RouterLink
            v-for="item in navs"
            :key="item.name"
            class="nav__link"
            :class="{ 'is-active': active === item.name }"
            :to="{ name: item.name }"
          >
            {{ item.label }}
          </RouterLink>
        </nav>

        <div class="masthead__actions">
          <RouterLink class="cta" :to="{ name: 'sell' }">发布闲置</RouterLink>

          <el-dropdown v-if="user.isLoggedIn" trigger="click" @command="onCommand">
            <button class="user" type="button">
              <span class="user__avatar" aria-hidden="true">{{ user.initial }}</span>
              <span class="user__name">{{ user.displayName }}</span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <RouterLink v-else class="login-link" :to="{ name: 'login' }">登录 / 注册</RouterLink>
        </div>
      </div>
    </header>

    <main class="page">
      <slot />
    </main>

    <footer class="footer">
      <div class="shell footer__inner">
        <span>二手集市 · 校园闲置交易平台</span>
        <span class="footer__note">把用不上的东西，交给下一个需要它的人</span>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.shell-root {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

/* ---------- 顶部黑板栏 ---------- */
.masthead {
  position: sticky;
  top: 0;
  z-index: 30;
  background: var(--board);
  color: var(--chalk);
  border-bottom: 3px solid var(--board-deep);
}

.masthead__inner {
  min-height: var(--h-masthead);
  display: flex;
  align-items: center;
  gap: 26px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.brand__mark {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  background: var(--highlight);
  color: var(--board-deep);
  font-size: 18px;
  font-weight: 800;
  border-radius: var(--r-sm);
}

.brand__text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.brand__text strong {
  font-size: 17px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 0.03em;
}

.brand__text em {
  font-style: normal;
  font-size: 11px;
  letter-spacing: 0.14em;
  color: var(--chalk-soft);
}

.nav {
  display: flex;
  gap: 2px;
  margin-right: auto;
}

.nav__link {
  padding: 7px 13px;
  font-size: 14px;
  font-weight: 600;
  color: var(--chalk-soft);
  border-radius: var(--r-sm);
  transition: color 0.15s ease, background-color 0.15s ease;
}

.nav__link:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.nav__link.is-active {
  color: #fff;
  box-shadow: inset 0 -2px 0 var(--highlight);
}

.masthead__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

/* 唯一使用荧光笔黄的元素：发布入口 */
.cta {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 700;
  color: var(--board-deep);
  background: var(--highlight);
  border-radius: var(--r-sm);
  box-shadow: 2px 2px 0 rgba(0, 0, 0, 0.28);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.cta:hover {
  transform: translate(-1px, -1px);
  box-shadow: 3px 3px 0 rgba(0, 0, 0, 0.32);
}

.cta:active {
  transform: translate(1px, 1px);
  box-shadow: 1px 1px 0 rgba(0, 0, 0, 0.28);
}

.user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  font-size: 14px;
  font-weight: 600;
  color: var(--chalk);
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.user:hover {
  background: rgba(255, 255, 255, 0.16);
}

.user__avatar {
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  font-size: 13px;
  font-weight: 700;
  color: var(--board-deep);
  background: var(--chalk);
  border-radius: 50%;
}

.login-link {
  font-size: 14px;
  font-weight: 600;
  color: var(--chalk);
  padding: 7px 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: var(--r-sm);
  transition: background-color 0.15s ease;
}

.login-link:hover {
  background: rgba(255, 255, 255, 0.12);
}

/* ---------- 内容 ---------- */
.page {
  flex: 1;
  padding: 34px 0 72px;
}

.footer {
  border-top: 1px solid var(--line);
  background: #eef1ea;
}

.footer__inner {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 18px;
  align-items: baseline;
  padding-top: 20px;
  padding-bottom: 20px;
  font-size: 13px;
  color: var(--ink-soft);
}

.footer__note {
  margin-left: auto;
}

/* ---------- 窄屏 ---------- */
@media (max-width: 880px) {
  .masthead__inner {
    flex-wrap: wrap;
    gap: 10px 16px;
    padding-top: 12px;
    padding-bottom: 12px;
  }

  .brand__text em {
    display: none;
  }

  .masthead__actions {
    margin-left: auto;
  }

  .nav {
    order: 3;
    width: 100%;
    margin-right: 0;
    overflow-x: auto;
    padding-bottom: 2px;
  }

  .page {
    padding: 24px 0 56px;
  }

  .footer__note {
    margin-left: 0;
  }
}
</style>
