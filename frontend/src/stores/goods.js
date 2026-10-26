import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { goodsApi } from '@/api';
import { useUserStore } from '@/stores/user';

/**
 * 后端没有「按 id 查商品」的接口，订单里又只有 goodsId。
 * 所以这里把「别人在卖的」和「我在卖的」两份列表合起来做成本地索引，
 * 商品详情页和订单列表都靠它把 id 还原成名称。
 */
export const useGoodsStore = defineStore('goods', () => {
  const others = ref([]);
  const mine = ref([]);
  const loading = ref(false);
  const loaded = ref(false);

  const index = computed(() => {
    const map = new Map();
    [...others.value, ...mine.value].forEach((item) => map.set(item.id, item));
    return map;
  });

  const categories = computed(() => {
    const set = new Set();
    others.value.forEach((item) => {
      const c = (item.category || '').trim();
      if (c) set.add(c);
    });
    return [...set].sort((a, b) => a.localeCompare(b, 'zh-Hans-CN'));
  });

  function nameOf(id) {
    return index.value.get(id)?.name || '';
  }

  function byId(id) {
    return index.value.get(Number(id)) || null;
  }

  async function loadAll() {
    loading.value = true;
    try {
      // /goods/mine 需要登录态：游客不带 token 会拿到 401，被 http.js 的拦截器
      // 直接踢到登录页（集市首页就变成登录页了）。所以未登录时不请求它。
      const loggedIn = useUserStore().isLoggedIn;
      const [othersList, mineList] = await Promise.all([
        goodsApi.others(),
        loggedIn ? goodsApi.mine() : Promise.resolve([])
      ]);
      others.value = Array.isArray(othersList) ? othersList : [];
      mine.value = Array.isArray(mineList) ? mineList : [];
      loaded.value = true;
    } finally {
      loading.value = false;
    }
  }

  /** 详情页可能在没加载过列表的情况下被直接打开（刷新/分享链接） */
  async function ensureLoaded() {
    if (!loaded.value) await loadAll();
  }

  async function refreshMine() {
    const list = await goodsApi.mine();
    mine.value = Array.isArray(list) ? list : [];
  }

  function removeMine(id) {
    mine.value = mine.value.filter((item) => item.id !== id);
  }

  return {
    others,
    mine,
    loading,
    loaded,
    index,
    categories,
    nameOf,
    byId,
    loadAll,
    ensureLoaded,
    refreshMine,
    removeMine
  };
});
