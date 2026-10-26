/** 价格：统一两位小数，带 ¥ */
export function money(value) {
  const n = Number(value);
  if (!Number.isFinite(n)) return '¥—';
  return '¥' + n.toFixed(2);
}

/** 纯数字部分，用于价格需要分字号排版时 */
export function moneyDigits(value) {
  const n = Number(value);
  return Number.isFinite(n) ? n.toFixed(2) : '—';
}

/** 商品首字，用作无图商品的“封面”字形 */
export function monogram(name) {
  const s = String(name ?? '').replace(/\s+/g, '');
  if (!s) return '闲';
  if (/^[A-Za-z0-9]/.test(s)) return s.slice(0, 2).toUpperCase();
  return s.slice(0, 2);
}

const KNOWN_CATEGORY_COLORS = {
  书籍: '#2f6f8f',
  教材: '#2f6f8f',
  课本: '#2f6f8f',
  文具: '#a8761f',
  宿舍生活用品: '#7a5ca8',
  生活用品: '#7a5ca8',
  日用品: '#7a5ca8',
  电子产品: '#1f7a6a',
  数码: '#1f7a6a',
  运动: '#b0553f',
  衣物: '#8a5a86'
};

const FALLBACK_COLORS = ['#2f6f8f', '#a8761f', '#7a5ca8', '#1f7a6a', '#b0553f', '#52685e'];

/** 分类固定配色，未知分类按名字哈希取色，保证同一个分类颜色始终一致 */
export function categoryColor(category) {
  const key = String(category ?? '').trim();
  if (!key) return FALLBACK_COLORS[5];
  if (KNOWN_CATEGORY_COLORS[key]) return KNOWN_CATEGORY_COLORS[key];
  let hash = 0;
  for (let i = 0; i < key.length; i += 1) {
    hash = (hash * 31 + key.charCodeAt(i)) % 100000;
  }
  return FALLBACK_COLORS[hash % FALLBACK_COLORS.length];
}

/** 订单状态：0 待发货 / 1 已发货 / 2 已完成 */
export const ORDER_STATUS = {
  0: { label: '待发货', tone: 'wait' },
  1: { label: '已发货', tone: 'transit' },
  2: { label: '已完成', tone: 'done' }
};

export function orderStatus(status) {
  return ORDER_STATUS[status] ?? { label: '未知状态', tone: 'wait' };
}

/** 后端返回 2026-10-11T13:38:58，直接截断，避免时区换算 */
export function formatDateTime(value) {
  if (!value) return '—';
  return String(value).replace('T', ' ').slice(0, 16);
}

export function formatDate(value) {
  if (!value) return '—';
  return String(value).slice(0, 10);
}
