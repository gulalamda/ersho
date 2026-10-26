<script setup>
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import AuthLayout from '@/components/AuthLayout.vue';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const user = useUserStore();

const formRef = ref(null);
const submitting = ref(false);
const form = reactive({ name: '', password: '' });

const rules = {
  name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    const vo = await user.login({ name: form.name.trim(), password: form.password });
    ElMessage.success(`欢迎回来，${vo.name}`);
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/';
    router.replace(redirect);
  } catch (error) {
    ElMessage.error(error.message || '登录失败，请重试');
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <AuthLayout>
    <h1 class="title">登录</h1>
    <p class="subtitle">用注册时的用户名和密码进入集市。</p>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      size="large"
      @submit.prevent="submit"
    >
      <el-form-item label="用户名" prop="name">
        <el-input v-model="form.name" placeholder="请输入用户名" autocomplete="username" />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          show-password
          autocomplete="current-password"
          @keyup.enter="submit"
        />
      </el-form-item>

      <el-button
        class="submit"
        type="primary"
        size="large"
        :loading="submitting"
        @click="submit"
      >
        登录
      </el-button>
    </el-form>

    <p class="switch">
      还没有账号？
      <RouterLink :to="{ name: 'register' }">注册一个</RouterLink>
    </p>
    <p class="switch switch--quiet">
      <RouterLink :to="{ name: 'market' }">先随便逛逛</RouterLink>
    </p>
  </AuthLayout>
</template>

<style scoped>
.title {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.01em;
}

.subtitle {
  margin-top: 6px;
  margin-bottom: 26px;
  font-size: 14px;
  color: var(--ink-soft);
}

.submit {
  width: 100%;
  margin-top: 4px;
  font-weight: 700;
}

.switch {
  margin-top: 20px;
  font-size: 14px;
  color: var(--ink-soft);
}

.switch a {
  font-weight: 700;
  color: var(--board);
  border-bottom: 2px solid var(--highlight);
}

.switch a:hover {
  color: var(--marker);
}

.switch--quiet {
  margin-top: 8px;
  font-size: 13px;
}

.switch--quiet a {
  font-weight: 600;
  color: var(--ink-soft);
  border-bottom: 1px solid var(--line-strong);
}
</style>
