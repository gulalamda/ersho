# 二手集市 · 前端

校园闲置交易平台的前端，技术栈 **Vue 3 + Vite + Vue Router + Pinia + Element Plus + Axios**。

## 开发

```bash
cd frontend
npm install
npm run dev
```

打开 http://localhost:5173 。开发服务器会把 `/api` 代理到 `http://localhost:8080`，
所以要先启动后端（`mvn spring-boot:run`）。

## 打包

```bash
cd frontend
npm run build
```

产物直接输出到 `../src/main/resources/static`，随后启动后端
（或在 IDEA 里运行 `ErshoApplication`），访问 http://localhost:8080/ 即可看到完整页面。

## 目录

```
src/
  api/        axios 实例 + 各模块接口
  stores/     Pinia：登录态、商品缓存
  router/     路由与登录守卫
  layouts/    整体框架（顶部导航）
  views/      页面
  components/ 复用组件（商品卡片等）
  styles/     设计变量与基础样式
  utils/      格式化与配色工具
```

## 和后端的约定

- 统一返回体 `{ code, msg, object }`：`code === 200` 取 `object`，否则按 `msg` 提示。
- 登录态放在请求头 `token` 里（后端 `tokenFilter` 读取的就是这个字段）。
- 接口 401 时前端会清掉本地 token 并跳回登录页。
