<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { studentApi } from '@/api';
import { useUserStore } from '@/stores/user';
import { formatDate } from '@/utils/format';

const router = useRouter();
const user = useUserStore();

const formRef = ref(null);
const loading = ref(false);
const saving = ref(false);
const failed = ref(false);

const form = reactive({
  name: '',
  gender: '',
  age: undefined,
  phone: '',
  address: '',
  entryDate: ''
});

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 20, message: '姓名最多 20 个字', trigger: 'blur' }
  ],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入 11 位手机号', trigger: 'blur' }]
};

function fill(profile) {
  form.name = profile.name || '';
  form.gender = profile.gender || '';
  form.age = profile.age ?? undefined;
  form.phone = profile.phone || '';
  form.address = profile.address || '';
  form.entryDate = profile.entryDate ? String(profile.entryDate).slice(0, 10) : '';
}

async function load() {
  loading.value = true;
  failed.value = false;
  try {
    const profile = await user.loadProfile(true);
    if (profile) fill(profile);
  } catch {
    failed.value = true;
  } finally {
    loading.value = false;
  }
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  saving.value = true;
  try {
    // 注意：这里刻意不提交 password。后端 PUT /student/put 不做加密，
    // 直接传密码会把库里的 BCrypt 哈希覆盖成明文。
    await user.updateProfile({
      name: form.name.trim(),
      gender: form.gender || null,
      age: form.age ?? null,
      phone: form.phone || null,
      address: form.address || null,
      entryDate: form.entryDate || null
    });
    ElMessage.success('资料已保存');
  } catch (error) {
    ElMessage.error(error.message || '保存失败，请重试');
  } finally {
    saving.value = false;
  }
}

async function removeAccount() {
  try {
    await ElMessageBox.confirm(
      '注销后账号和登录资格会一起删除，已经产生的订单不会自动取消。这一步不能撤销。',
      '注销账号',
      { confirmButtonText: '确认注销', cancelButtonText: '我再想想', type: 'warning' }
    );
  } catch {
    return;
  }
  try {
    await studentApi.remove();
    user.logout();
    ElMessage.success('账号已注销');
    router.replace({ name: 'login' });
  } catch (error) {
    ElMessage.error(error.message || '注销失败，请重试');
  }
}

onMounted(load);
</script>

<template>
  <div class="shell profile">
    <header class="head">
      <h1 class="head__title">个人资料</h1>
      <p class="head__lead">
        姓名和手机号会跟着商品一起展示给买家，方便对方联系你。
      </p>
    </header>

    <div v-if="failed" class="fail">
      <p class="fail__title">没能拿到你的资料</p>
      <p class="fail__hint">登录可能已经过期，重新登录一次就好。</p>
      <el-button type="primary" @click="load">重新加载</el-button>
    </div>

    <div v-else class="cols">
      <section v-loading="loading" class="panel">
        <h2 class="panel__title">修改资料</h2>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          @submit.prevent="submit"
        >
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" maxlength="20" />
          </el-form-item>

          <div class="row">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="未填写" clearable>
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="15" :max="60" controls-position="right" />
            </el-form-item>
          </div>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" maxlength="11" placeholder="11 位手机号" />
          </el-form-item>

          <el-form-item label="收货地址 / 宿舍" prop="address">
            <el-input v-model="form.address" maxlength="60" placeholder="例如：3 号楼 502" />
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

          <el-button type="primary" size="large" :loading="saving" @click="submit">
            保存修改
          </el-button>
        </el-form>
      </section>

      <aside class="side">
        <section class="card">
          <h2 class="card__title">账号</h2>
          <dl class="facts">
            <div>
              <dt>用户名</dt>
              <dd>{{ user.profile?.name || '—' }}</dd>
            </div>
            <div>
              <dt>学生编号</dt>
              <dd class="num">{{ user.profile?.id ?? '—' }}</dd>
            </div>
            <div>
              <dt>入学日期</dt>
              <dd class="num">{{ formatDate(user.profile?.entryDate) }}</dd>
            </div>
          </dl>
        </section>

        <section class="card card--danger">
          <h2 class="card__title">注销账号</h2>
          <p class="card__body">
            注销会删除你的登录账号，正在进行的订单不会自动取消，需要先和对方说一声。
          </p>
          <button class="danger" type="button" @click="removeAccount">注销账号</button>
        </section>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.profile {
  max-width: 1000px;
}

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

.cols {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 24px;
  margin-top: 26px;
  align-items: start;
}

.panel {
  padding: 26px 24px 22px;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--r-sm);
  box-shadow: var(--shadow-tag);
}

.panel__title {
  margin-bottom: 18px;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.row :deep(.el-input-number),
.row :deep(.el-select) {
  width: 100%;
}

.side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  padding: 20px;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--r-sm);
}

.card__title {
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.facts {
  margin: 14px 0 0;
}

.facts div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 7px 0;
  border-bottom: 1px dashed var(--line);
}

.facts div:last-child {
  border-bottom: 0;
}

.facts dt {
  font-size: 13px;
  color: var(--ink-soft);
}

.facts dd {
  margin: 0;
  font-size: 13.5px;
  font-weight: 600;
  text-align: right;
  word-break: break-all;
}

.card--danger {
  border-color: var(--marker-soft);
  background: #fdf7f6;
}

.card--danger .card__title {
  color: var(--marker);
}

.card__body {
  margin: 10px 0 16px;
  font-size: 13px;
  line-height: 1.75;
  color: var(--ink-soft);
}

.danger {
  width: 100%;
  padding: 9px 0;
  font-family: inherit;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--marker);
  background: transparent;
  border: 1px solid var(--marker);
  border-radius: var(--r-sm);
  cursor: pointer;
  transition: color 0.15s ease, background-color 0.15s ease;
}

.danger:hover {
  color: var(--card);
  background: var(--marker);
}

.fail {
  margin-top: 26px;
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

@media (max-width: 860px) {
  .cols {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .row {
    grid-template-columns: 1fr;
    gap: 0;
  }
}
</style>
