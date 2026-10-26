# 二手集市（ersho）

毕业设计项目：校园闲置交易平台。Spring Boot 单体应用，**前端页面打包后直接塞进 jar**，所以部署只需要一个容器。

- 后端：Spring Boot 2.7.18 + MyBatis-Plus 3.5.7 + MySQL 8 + JWT
- 前端：Vue 3 + Vite + Element Plus + Pinia
- 部署：Docker + Docker Compose
- 运行环境：Java 17+

## 目录结构

```
ersho/
├── src/main/java/org/example/ersho/
│   ├── controller/     接口层（goods / orders / student）
│   ├── Service/        业务层
│   ├── mapper/         MyBatis-Plus Mapper（继承 BaseMapper）
│   ├── entity/         商品、订单实体
│   ├── entityDto/      学生实体、登录 DTO、统一返回体
│   ├── filter/         JWT 校验过滤器
│   └── config/         Spring Security 配置
├── src/main/resources/
│   ├── application.yml 配置（支持环境变量覆盖）
│   ├── mapper/*.xml    MyBatis 映射文件
│   └── static/         前端构建产物（由 frontend 打包生成）
├── frontend/           Vue 3 源码
├── deploy/             部署脚本、初始化 SQL、部署文档
├── Dockerfile          两阶段构建镜像
└── docker-compose.yml  编排文件
```

## 本地开发

```bash
# 1. 起数据库（本机 MySQL 建 tlias 库，导入表结构）
mysql -uroot -p < deploy/sql/01-init.sql

# 2. 起后端（默认 http://localhost:8080/api/）
./mvnw -s settings.xml spring-boot:run
# Windows: .\mvnw.cmd -s settings.xml spring-boot:run

# 3. 起前端（http://localhost:5173，/api 自动转发到 8080）
cd frontend && npm install && npm run dev
```

## 打包

```bash
# 改了前端必须先 build，产物会直接写进 src/main/resources/static
cd frontend && npm run build && cd ..

# 再打 jar
./mvnw -s settings.xml clean package -DskipTests
java -jar target/ersho-0.0.1-SNAPSHOT.jar
```

> ⚠️ **只改 Vue 不跑 `npm run build`，打出来的 jar 里还是旧页面。**
> `src/main/resources/static/` 随仓库提供，修改前端源码后需要重新构建。

## 部署到服务器

完整步骤见 **[deploy/README.md](deploy/README.md)**，一键脚本：

```bash
# 在目标机器上
bash deploy/deploy.sh
```

## 关于 `/api` 前缀

前端 axios 的 `baseURL` 是 `/api`。开发时靠 Vite 代理转发，打包后用 `server.servlet.context-path=/api` 让整个应用挂在 `/api` 下，两边路径一致，前端不用改也不用重新构建。

部署后请直接访问 `http://服务器IP:端口/api/`。

## 演示账号

| 手机号 | 姓名 | 密码 |
|---|---|---|
| 13900139000 | 张三 | 123456 |
| 13700137000 | 李四 | 123456 |
| 13600136000 | 王佳 | 123456 |
| 13500135000 | 陈明 | 123456 |
| 13400134000 | 刘婷 | 123456 |
| 13300133000 | 赵宇 | 123456 |
| 13200132000 | 周欣 | 123456 |

## 配置与商品图片

数据库配置通过 `MYSQL_URL`、`MYSQL_USERNAME`、`MYSQL_PASSWORD` 环境变量覆盖；本地默认连接 localhost:3306/tlias，账号和密码均为 root，仅用于本地演示。

JWT 密钥通过 `JWT_SECRET` 环境变量设置，至少 32 字节。不设置会为本次启动生成随机密钥，重启后需重新登录。Docker 部署可复制 `deploy/.env.example` 到 `.env` 并填写配置；`.env` 不上传。直接运行 Java 时需在终端或 IDE 中配置环境变量。

全新数据库执行 `deploy/sql/01-init.sql`，已有旧数据库执行 `deploy/sql/02-goods-images.sql`。升级脚本先备份 goods，再缩减每类数据，并保留历史订单关联。演示数据为书籍、文具、生活用品各 9 件。

商品图片为本地 SVG 示意图，源码位于 `frontend/public/images/goods/`；构建后由后端 `/api/images/goods/` 提供。发布和编辑商品支持填写图片链接并预览。
