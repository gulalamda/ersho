<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { goodsApi } from '@/api';
import { useGoodsStore } from '@/stores/goods';
import GoodsImage from '@/components/GoodsImage.vue';
import EmptyState from '@/components/EmptyState.vue';

const props = defineProps({ id: { type: [String, Number], default: null } });

const router = useRouter();
const goods = useGoodsStore();

const isEdit = computed(() => props.id != null);
const loading = ref(false);
const submitting = ref(false);
const notFound = ref(false);
const formRef = ref(null);

const form = reactive({
  name: '',
  category: '',
  price: undefined,
  stock: 1,
  description: '',
  imageUrl: ''
});

const rules = {
  imageUrl: [{ validator: (_rule, value, callback) => {
    if (value && (!/^(https?:\/\/|\/(?!\/))/.test(value.trim()) || value.trim().length > 500)) callback(new Error('请输入 http(s) 图片链接或 / 开头的本地图片路径，最多 500 字符'));
    else callback();
  }, trigger: 'blur' }],
  name: [
    { required: true, message: '请填写商品名称', trigger: 'blur' },
    { max: 60, message: '名称最多 60 个字', trigger: 'blur' }
  ],
  category: [{ required: true, message: '请选择或填写分类', trigger: 'change' }],
  price: [
    { required: true, message: '请填写价格', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value == null || Number(value) <= 0) callback(new Error('价格要大于 0'));
        else callback();
      },
      trigger: 'blur'
    }
  ],
  stock: [
    { required: true, message: '请填写库存', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value == null || Number(value) < 1) callback(new Error('库存至少 1 件'));
        else callback();
      },
      trigger: 'blur'
    }
  ]
};

// 常用分类放在前面，同时允许自己写一个新的
const presetCategories = ['书籍', '文具', '宿舍生活用品', '电子产品', '运动', '其他'];
const categoryOptions = computed(() => {
  const set = new Set(presetCategories);
  goods.others.forEach((item) => {
    const value = (item.category || '').trim();
    if (value) set.add(value);
  });
  return [...set];
});

async function loadForEdit() {
  loading.value = true;
  notFound.value = false;
  try {
    await goods.ensureLoaded();
    const item = goods.byId(props.id);
    if (!item) {
      notFound.value = true;
      return;
    }
    form.name = item.name || '';
    form.category = item.category || '';
    form.price = item.price != null ? Number(item.price) : undefined;
    form.stock = item.stock != null ? Number(item.stock) : 1;
    form.description = item.description || '';
    form.imageUrl = item.imageUrl || '';
  } finally {
    loading.value = false;
  }
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  const payload = {
    name: form.name.trim(),
    category: form.category.trim(),
    price: Number(form.price),
    stock: Number(form.stock),
    description: form.description.trim() || null,
    imageUrl: form.imageUrl.trim() || null
  };

  try {
    if (isEdit.value) {
      await goodsApi.update({ ...payload, id: Number(props.id) });
      ElMessage.success('已保存修改');
    } else {
      await goodsApi.publish(payload);
      ElMessage.success('已上架，去集市看看');
    }
    goods.loaded = false;
    await goods.loadAll();
    router.push({ name: 'my-goods' });
  } catch (error) {
    ElMessage.error(error.message || '保存失败，请重试');
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  goods.ensureLoaded().catch(() => {});
  if (isEdit.value) loadForEdit();
});
</script>

<template>
  <div class="shell sell">
    <header class="head">
      <h1 class="head__title">{{ isEdit ? '编辑商品' : '发布闲置' }}</h1>
      <p class="head__lead">
        {{ isEdit
          ? '改完直接保存，集市上的信息会立刻更新。'
          : '写清楚是什么、几成新、怎么取，买的人会快很多。' }}
      </p>
    </header>

    <EmptyState
      v-if="notFound"
      title="找不到这件商品"
      hint="它可能已经被你下架了。去「我的在售」看看现在还有哪些。"
      action-text="我的在售"
      :action-to="{ name: 'my-goods' }"
    />

    <div v-else v-loading="loading" class="panel">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @submit.prevent="submit"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：高等数学 第七版 上册" maxlength="60" show-word-limit />
        </el-form-item>

        <div class="row">
          <el-form-item label="分类" prop="category">
            <el-select
              v-model="form.category"
              placeholder="选一个，或者直接输入新的"
              filterable
              allow-create
              default-first-option
            >
              <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>

          <el-form-item label="价格（元）" prop="price">
            <el-input-number
              v-model="form.price"
              :min="0.01"
              :max="99999"
              :precision="2"
              :step="1"
              controls-position="right"
            />
          </el-form-item>

          <el-form-item label="库存（件）" prop="stock">
            <el-input-number
              v-model="form.stock"
              :min="1"
              :max="9999"
              controls-position="right"
            />
          </el-form-item>
        </div>

        <el-form-item label="商品图片地址" prop="imageUrl">
          <el-input v-model="form.imageUrl" maxlength="500" clearable placeholder="https://… 或 /api/images/goods/1.svg" />
        </el-form-item>
        <div v-if="form.imageUrl" style="margin-bottom: 20px"><GoodsImage :goods="form" /></div>

        <el-form-item label="商品说明" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="几成新、有没有笔记划线、什么时候方便自提……"
          />
        </el-form-item>

        <div class="actions">
          <el-button type="primary" size="large" :loading="submitting" @click="submit">
            {{ isEdit ? '保存修改' : '确认上架' }}
          </el-button>
          <el-button size="large" @click="router.back()">取消</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.sell {
  max-width: 780px;
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

.panel {
  margin-top: 26px;
  padding: 28px 26px 22px;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--r-sm);
  box-shadow: var(--shadow-tag);
}

.row {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr;
  gap: 16px;
}

.row :deep(.el-input-number) {
  width: 100%;
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

@media (max-width: 720px) {
  .row {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .panel {
    padding: 22px 18px 16px;
  }
}
</style>
