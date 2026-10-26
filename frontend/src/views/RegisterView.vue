<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import AuthLayout from '@/components/AuthLayout.vue';
import { studentApi } from '@/api';

const router = useRouter();
const formRef = ref(null);
const submitting = ref(false);

const form = reactive({
  name: '',
  password: '',
  confirm: '',
  gender: '男',
  age: undefined,
  phone: '',
  address: '',
  entryDate: ''
});

const rules = {
  name: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度 2-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.password) callback(new Error('两次输入的密码不一致'));
        else callback();
      },
      trigger: 'blur'
    }
  ],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入 11 位手机号',
      trigger: 'blur'
    }
  ]
};

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    await studentApi.register({
      name: form.name.trim(),
      password: form.password,
      gender: form.gender,
      age: form.age ?? null,
      phone: form.phone || null,
      address: form.address || null,
      entryDate: form.entryDate || null
    });
    ElMessage.success('注册成功，请登录');
    router.replace({ name: 'login' });
  } catch (error) {
    ElMessage.error(error.message || '注册失败，请重试');
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <AuthLayout>
    <h1 class="title">注册</h1>
    <p class="subtitle">填好联系方式，买家才能找到你。带 <b>*</b> 的是必填。</p>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      size="large"
      @submit.prevent="submit"
    >
      <el-form-item label="用户名" prop="name">
        <el-input v-model="form.name" placeholder="登录时使用，别重复" autocomplete="username" />
      </el-form-item>

      <div class="row">
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="至少 6 位" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirm">
          <el-input v-model="form.confirm" type="password" placeholder="再输一次" show-password />
        </el-form-item>
      </div>

      <div class="row">
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="form.age" :min="15" :max="60" controls-position="right" />
        </el-form-item>
      </div>

      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="方便买家联系你" maxlength="11" />
      </el-form-item>

      <el-form-item label="收货地址 / 宿舍" prop="address">
        <el-input v-model="form.address" placeholder="例如：3 号楼 502" />
      </el-form-item>

      <el-form-item label="入学日期" prop="entryDate">
        <el-date-picker
          v-model="form.entryDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-button class="submit" type="primary" size="large" :loading="submitting" @click="submit">
        注册
      </el-button>
    </el-form>

    <p class="switch">
      已经注册过了？
      <RouterLink :to="{ name: 'login' }">直接登录</RouterLink>
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
  margin-bottom: 22px;
  font-size: 14px;
  color: var(--ink-soft);
}

.subtitle b {
  color: var(--marker);
}

.row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.row :deep(.el-input-number) {
  width: 100%;
}

.submit {
  width: 100%;
  margin-top: 4px;
  font-weight: 700;
}

.switch {
  margin-top: 18px;
  font-size: 14px;
  color: var(--ink-soft);
}

.switch a {
  font-weight: 700;
  color: var(--board);
  border-bottom: 2px solid var(--highlight);
}

@media (max-width: 520px) {
  .row {
    grid-template-columns: 1fr;
    gap: 0;
  }
}
</style>
