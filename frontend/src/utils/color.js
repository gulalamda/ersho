/** #14453a + 0.12 -> rgba(20,69,58,0.12)，避免依赖 color-mix 的浏览器支持 */
export function hexToRgba(hex, alpha = 1) {
  const raw = String(hex ?? '').replace('#', '').trim();
  const full = raw.length === 3 ? raw.split('').map((c) => c + c).join('') : raw;
  const n = Number.parseInt(full, 16);
  if (!Number.isFinite(n) || full.length !== 6) return `rgba(82, 104, 94, ${alpha})`;
  const r = (n >> 16) & 255;
  const g = (n >> 8) & 255;
  const b = n & 255;
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}
