# 部署到 192.168.136.131（Docker）

二手集市（ersho）整套系统：Spring Boot 打成一个 jar 塞进容器，**前端页面已经打包在 jar 里**（`src/main/resources/static`），所以只需要**一个应用容器**，不用单独部署 Nginx。

```
浏览器  http://192.168.136.131:8081/
          │
          ▼
┌─────────────────────────────┐
│ 192.168.136.131 (VM)        │
│                             │
│  ┌───────────────────────┐  │        ┌──────────────────┐
│  │ ersho-app 容器         │──┼───────▶│ 宿主机 MySQL 8    │
│  │ Tomcat 8080 → 宿主 8081│  │  3306  │ 库：tlias        │
│  │ context-path = /api    │  │        └──────────────────┘
│  └───────────────────────┘  │
└─────────────────────────────┘
```

---

## 一、先搞清楚一件事：为什么 URL 是 `/api/`

前端 axios 的 `baseURL` 写死是 `/api`。开发的时候 Vite 用 `rewrite` 把 `/api` 前缀抹掉了，所以后端接口看起来是 `/goods/select`。

但打成 jar 之后没有 Vite 了，如果不管，浏览器请求 `/api/goods/select` 会直接 **404**。

所以 `application.yml` 里加了：

```yaml
server:
  servlet:
    context-path: ${SERVER_CONTEXT_PATH:/api}
```

整个应用挂在 `/api` 下，`/api/goods/select` 正好命中 `@RequestMapping("/goods")`。这样**前端一行代码不用改、也不用重新 build**。

副作用只有一个：静态页面也在 `/api` 下，所以访问地址是 `http://192.168.136.131:8081/`（会被 302 到 `/api/`），你直接把根地址给老师就行。

---

## 二、部署步骤

### 0. 前置检查

在 VM 上确认这三样：

```bash
docker version                 # Docker 装了吗
docker compose version         # compose 插件有吗
systemctl status mysqld        # MySQL 在跑吗
mysql --version                # 版本最好 8.0
```

### 1. 把项目传到 VM

在 **Windows 本机**执行（注意 `target/` 不用传，容器里会重新编译）：

```powershell
# 方式一：scp
scp -r Dockerfile docker-compose.yml .env deploy src pom.xml root@192.168.136.131:/opt/ersho/

# 方式二：已经有 git 仓库
# 在 VM 上 git clone 你的仓库地址 /opt/ersho
```

VM 上建目录（如果 scp 报目录不存在）：

```bash
mkdir -p /opt/ersho
```

### 2. 初始化数据库（关键一步）

```bash
cd /opt/ersho
bash deploy/db-init.sh
```

这个脚本会：
- 建 `tlias` 库 + `goods`/`orders`/`student` 三张表 + 演示数据
- 建一个只授权 `tlias` 库的远程账号 `ersho/ersho123`
- **自动把 `bind-address` 从 `127.0.0.1` 改成 `0.0.0.0`** 并重启 MySQL

> ⚠️ 第三步最容易踩坑：MySQL 默认 `bind-address = 127.0.0.1`，只监听本机回环。容器从宿主机网卡过来会被拒绝，报 `Communications link failure`。脚本会自动处理并备份原配置。

跑完应该看到：

```
初始化完成
状态      学生数  商品数  订单数
初始化完成  7      50     9
```

### 3. 构建并启动应用

```bash
cd /opt/ersho
docker compose up -d --build
```

第一次构建要下载 Maven 依赖，**5~15 分钟**（取决于网速）。之后改 Java 代码重新构建只要 1 分钟左右。

看日志：

```bash
docker compose logs -f app
```

出现这行就成功了：

```
Started ErshoApplication in 3.597 seconds
```

### 4. 验证

```bash
# 容器状态应该是 healthy
docker compose ps

# 探活
curl -I http://127.0.0.1:8081/
```

浏览器打开 **http://192.168.136.131:8081/**，用演示账号登录：

| 手机号 | 姓名 | 密码 |
|---|---|---|
| 13900139000 | 张三 | 123456 |
| 13700137000 | 李四 | 123456 |
| 13600136000 | 王佳 | 123456 |
| 13500135000 | 陈明 | 123456 |
| 13400134000 | 刘婷 | 123456 |
| 13300133000 | 赵宇 | 123456 |
| 13200132000 | 周欣 | 123456 |

---

## 三、常用运维命令

```bash
docker compose ps                        # 查看状态
docker compose logs -f app               # 跟踪日志
docker compose restart app               # 重启应用
docker compose down                      # 停止并删除容器（数据不受影响）
docker compose up -d --build             # 改完代码重新构建

# 进容器里看看
docker exec -it ersho-app sh
```

改端口 / 改数据库密码：编辑 `.env` 后 `docker compose up -d`（**不用 rebuild**）。

改 Java 代码 / 改前端：**必须** `docker compose up -d --build`，因为代码是编译进 jar 再进镜像的。

---

## 四、排错

### 启动报 `Communications link failure`

容器连不上 MySQL。按顺序查：

```bash
# 1. 宿主机上 MySQL 在监听吗，绑的是哪个地址
ss -lntp | grep 3306
# 期望看到 0.0.0.0:3306 或 *:3306
# 如果只有 127.0.0.1:3306 → 回到第 2 步跑 db-init.sh，或手动改 bind-address

# 2. VM 防火墙放行了吗
sudo firewall-cmd --permanent --add-port=3306/tcp && sudo firewall-cmd --reload   # CentOS
sudo ufw allow 3306/tcp                                                          # Ubuntu

# 3. 从容器内部测一下能不能通宿主机
docker exec -it ersho-app sh -c "curl -v telnet://host.docker.internal:3306"
```

还是不行就把 `.env` 里的 `DB_HOST` 换成 VM 的真实 IP `192.168.136.131` 试试。

### 启动报 `Access denied for user 'ersho'@'172.17.0.x'`

账号没建好，或者密码和 `.env` 对不上：

```bash
mysql -uroot -p -e "SELECT user, host FROM mysql.user WHERE user IN ('ersho','root');"
mysql -uroot -p -e "SHOW GRANTS FOR 'ersho'@'%';"
```

### 报 `Public Key Retrieval is not allowed`

连接串里少了 `allowPublicKeyRetrieval=true`。`docker-compose.yml` 里的 `MYSQL_URL` 已经带上了，别手动删。

### 页面能打开但接口全 404

说明 `context-path` 没生效。检查：

```bash
docker compose logs app | grep "context path"
# 期望：Tomcat started on port(s): 8080 (http) with context path '/api'
```

浏览器 F12 看 Network，请求应该是 `http://192.168.136.131:8081/api/goods/select`。如果请求里没有 `/api`，那是前端 build 版本不对。

### 构建慢 / 卡在 maven 下载

容器里用的是 Maven 中央仓库，国内会慢。可以在 `Dockerfile` 的 builder 阶段换成阿里云镜像：

```dockerfile
RUN mkdir -p /root/.m2 && cat > /root/.m2/settings.xml <<'EOF'
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
EOF
```

### 8081 端口被占用

```bash
sudo ss -lntp | grep 8081
# 改 .env 里的 APP_PORT，比如改成 8082
```

---

## 五、备选：VM 上没有 MySQL

如果目标机没装 MySQL，用容器版数据库：

```bash
# 1. 起数据库（首次会自动执行 deploy/sql/01-init.sql）
docker compose -f deploy/docker-compose.db.yml up -d

# 2. 改 .env，把数据库指向容器
#    DB_HOST=mysql
#    DB_USERNAME=ersho
#    DB_PASSWORD=ersho123

# 3. 起应用
docker compose up -d --build
```

两个 compose 文件共用 `ersho-net` 网络，所以应用能直接用服务名 `mysql` 连过去。

---

## 六、答辩前 checklist

- [ ] `docker compose ps` 里 app 是 `healthy`
- [ ] 浏览器打开 `http://192.168.136.131:8081/` 有登录页，不是白屏
- [ ] 用 `13900139000 / 123456` 能登录
- [ ] 能搜索商品、能上架、能下单（这三条走完整链路）
- [ ] VM 关机重启后容器自动起来（`restart: unless-stopped` 已配好）
- [ ] 把 `deploy/` 和 `Dockerfile` 一起交到毕设材料里，证明会容器化部署

商品图片：goods.image_url 对应接口的 imageUrl；列表、详情、发布和编辑页均已接入。
演示图片位于 frontend/public/images/goods，构建时自动复制到后端静态资源。
全新数据库执行 01-init.sql；已有数据库先执行 02-goods-images.sql。
02 会先备份到 goods_backup_before_images，再将每类商品缩减到最多 9 件，并保留订单引用。
如果某类有超过 9 件商品已被订单引用，会保留它们，避免历史订单丢失。
本次本地升级后三个原有分类各 9 件，另保留运动分类的 1 件自建商品。
发布和编辑可填写图片地址，清空可移除图片；无图或图片加载失败时显示文字占位。
