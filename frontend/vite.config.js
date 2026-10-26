import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 开发时前端跑在 5173，接口通过 /api 转发到 Spring Boot 的 8080，
// 这样前后端同源，不需要后端开 CORS。
// 注意：后端 context-path 就是 /api，所以这里不能再 rewrite 掉前缀，
// 否则转发过去变成 /goods/select，会 404。
export default defineConfig(({ command }) => {
  const isBuild = command === 'build';

  return {
  // 打包后的页面是挂在后端 context-path /api 下访问的，index.html 里的资源
  // 必须写成 /api/assets/...，否则经 nginx / cloudflared 这类反向代理访问时
  // /assets/* 会 404（浏览器请求的是站点根路径，而资源实际在 /api 下面）。
  // 开发服务器仍用 '/'，不然 vite 的 /api 代理会被自己的资源请求抢走。
  base: isBuild ? '/api/' : '/',

  // 这台机器上 esbuild 有个死结：它的 JS API 对超过 1MB 的输入会先在临时目录
  // 写 esbuild-xxx 文件、再让 esbuild.exe 删掉，而卡巴斯基/360 会拦这次删除，
  // 报 remove ...: Access is denied.，整个 vite build 就失败。
  // 所以打包时干脆不让 esbuild 处理 chunk：
  //   esbuild: false        —— 关掉 chunk 转译（项目是纯 JS + Vue，不需要降级）
  //   build.minify: terser —— 改用 JS 实现的压缩器，不碰临时文件
  // 开发模式保持原样，不受影响。
  ...(isBuild ? { esbuild: false } : {}),

  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
    chunkSizeWarningLimit: 1200,
    minify: 'terser',
    // CSS 还是让 esbuild 压缩（CSS 只有 360KB 左右，碰不到 1MB 那条临时文件通道）
    cssMinify: 'esbuild'
  }
  };
})
