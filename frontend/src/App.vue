<script setup>
import { computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElConfigProvider } from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import AppShell from '@/layouts/AppShell.vue';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const user = useUserStore();

const bare = computed(() => route.meta.layout === 'bare');

// 刷新页面后 token 还在，但内存里的资料没了，这里补一次
onMounted(() => {
  if (user.isLoggedIn) user.loadProfile().catch(() => {});
});
</script>

<template>
  <!-- 组件库改成按需引入后，中文语言包要靠 ConfigProvider 注入，
       否则日期选择器、确认弹窗的按钮文案会变成英文。 -->
  <ElConfigProvider :locale="zhCn">
    <RouterView v-if="bare" />
    <AppShell v-else>
      <RouterView />
    </AppShell>
  </ElConfigProvider>
</template>
