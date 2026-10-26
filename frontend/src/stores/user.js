import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { studentApi } from '@/api';
import { clearToken, getToken, setToken } from '@/api/http';

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken());
  const profile = ref(null);
  const loading = ref(false);

  const isLoggedIn = computed(() => Boolean(token.value));
  const displayName = computed(() => profile.value?.name || '同学');
  const initial = computed(() => String(displayName.value).replace(/\s+/g, '').slice(0, 1) || '同');
  const address = computed(() => profile.value?.address || '');

  /** 登录：拿到 jwt 后立刻补一次用户资料，后续下单要用到宿舍地址 */
  async function login(credentials) {
    const vo = await studentApi.login(credentials);
    setToken(vo.jwt);
    token.value = vo.jwt;
    profile.value = { id: vo.id, name: vo.name };
    try {
      await loadProfile(true);
    } catch {
      /* 资料拉取失败不影响登录态 */
    }
    return vo;
  }

  async function loadProfile(force = false) {
    if (!token.value) return null;
    if (profile.value?.phone !== undefined && !force) return profile.value;
    loading.value = true;
    try {
      profile.value = await studentApi.info();
      return profile.value;
    } finally {
      loading.value = false;
    }
  }

  async function updateProfile(payload) {
    await studentApi.update(payload);
    await loadProfile(true);
  }

  function logout() {
    clearToken();
    token.value = '';
    profile.value = null;
  }

  return {
    token,
    profile,
    loading,
    isLoggedIn,
    displayName,
    initial,
    address,
    login,
    loadProfile,
    updateProfile,
    logout
  };
});
