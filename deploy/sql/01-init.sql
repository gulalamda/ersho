-- ============================================================
-- 二手集市（ersho）数据库初始化脚本
--
-- 用途：在 192.168.136.131 这台机器上把 tlias 库、三张表、
--       演示数据和远程登录账号一次性建好。
--
-- 执行（在 VM 上）：
--   mysql -uroot -p < deploy/sql/01-init.sql
--
-- 脚本是幂等的：库和表用 IF NOT EXISTS，数据用 INSERT IGNORE，
-- 重复执行不会报错也不会产生重复数据。已存在的库不会被清空。
--
-- 注意：DDL 里的 COLLATE utf8mb4_0900_ai_ci 需要 MySQL 8.0+。
--       如果目标机是 5.7，把这三处的 COLLATE 换成 utf8mb4_general_ci。
-- ============================================================

SET NAMES utf8mb4;

-- ---------- 1. 建库 ----------
CREATE DATABASE IF NOT EXISTS `tlias`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `tlias`;

-- ---------- 2. 远程登录账号 ----------
-- 说明：MySQL 的 root 默认只允许 root@localhost 登录，
--       容器从宿主机网卡过来会被当成远程连接，所以专门建一个账号。
--       密码请按需修改，并同步改 .env 里的 DB_PASSWORD。
CREATE USER IF NOT EXISTS 'ersho'@'%' IDENTIFIED BY 'ersho123';
GRANT ALL PRIVILEGES ON `tlias`.* TO 'ersho'@'%';

-- 顺带放开 root 远程（有些环境默认没有，留作备用；不需要可以删掉这两行）
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY 'root';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;

FLUSH PRIVILEGES;

-- ---------- 3. 建表 ----------
CREATE TABLE IF NOT EXISTS `goods` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `category` varchar(50) NOT NULL COMMENT '分类：书籍/文具/宿舍生活用品',
  `price` decimal(8,2) NOT NULL COMMENT '商品价格',
  `stock` int DEFAULT '0' COMMENT '库存数量',
  `description` varchar(200) DEFAULT NULL COMMENT '商品简介',
  `seller_id` int DEFAULT NULL COMMENT '上架人用户id',
  `image_url` varchar(500) DEFAULT NULL COMMENT '商品图片地址',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生商品表';

CREATE TABLE IF NOT EXISTS `orders` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '订单id',
  `order_no` varchar(32) NOT NULL COMMENT '订单编号',
  `goods_id` int NOT NULL COMMENT '商品id',
  `buyer_id` int NOT NULL COMMENT '买家id(关联student.id)',
  `seller_id` int NOT NULL COMMENT '卖家id(关联student.id)',
  `address` varchar(255) NOT NULL COMMENT '收货地址',
  `price` decimal(8,2) NOT NULL COMMENT '成交价格',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态:0未进行 1进行中 2发货与收货完成',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `num` int NOT NULL DEFAULT '0' COMMENT '购买数量',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';

CREATE TABLE IF NOT EXISTS `student` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '学生编号',
  `name` varchar(20) NOT NULL COMMENT '姓名',
  `gender` char(2) DEFAULT NULL COMMENT '性别',
  `age` tinyint DEFAULT NULL COMMENT '年龄',
  `address` varchar(100) DEFAULT NULL COMMENT '住址',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机号',
  `entry_date` date DEFAULT NULL COMMENT '入学日期',
  `password` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



-- ---------- 4. 演示数据 ----------
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (1,'高等数学教材','书籍',39.80,120,'大学高数课本',7, '/api/images/goods/1.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (2,'大学英语四级词汇','书籍',25.50,199,'四级备考单词书',7, '/api/images/goods/2.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (3,'Java编程入门','书籍',49.00,85,'Java基础学习教材',5, '/api/images/goods/3.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (4,'数据结构与算法','书籍',45.00,76,'计算机专业课本',3, '/api/images/goods/4.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (5,'现代汉语','书籍',32.00,83,'大学语文教材',7, '/api/images/goods/5.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (6,'Python零基础教程','书籍',42.50,68,'Python入门学习',6, '/api/images/goods/6.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (7,'四六级真题试卷','书籍',29.90,150,'英语四六级刷题卷',6, '/api/images/goods/7.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (9,'红楼梦','书籍',36.00,39,'经典文学读物',2, '/api/images/goods/9.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (15,'考研英语阅读','书籍',35.00,71,'考研备考用书',5, '/api/images/goods/15.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (16,'黑色按动中性笔','文具',2.50,500,'学生书写签字笔',6, '/api/images/goods/16.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (17,'A5笔记本','文具',8.90,320,'课堂记事笔记本',5, '/api/images/goods/17.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (18,'2B铅笔','文具',1.50,450,'考试涂卡铅笔',3, '/api/images/goods/18.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (19,'橡皮','文具',1.00,599,'绘图考试橡皮',5, '/api/images/goods/19.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (20,'尺子套装','文具',5.90,260,'直尺三角板量角器',2, '/api/images/goods/20.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (21,'订书机+订书钉','文具',12.00,180,'文件装订',7, '/api/images/goods/21.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (22,'便利贴','文具',3.50,400,'笔记标记贴',7, '/api/images/goods/22.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (23,'荧光笔套装','文具',9.90,220,'划重点标记笔',1, '/api/images/goods/23.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (24,'笔袋','文具',15.00,130,'文具收纳袋',6, '/api/images/goods/24.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (33,'宿舍床上小桌子','生活用品',35.00,75,'床上看书电脑桌',1, '/api/images/goods/33.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (34,'遮光床帘','生活用品',48.00,66,'宿舍隐私遮光帘',3, '/api/images/goods/34.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (35,'耳塞+眼罩套装','生活用品',12.90,210,'宿舍降噪助眠',5, '/api/images/goods/35.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (36,'宿舍小台灯','生活用品',29.90,92,'桌面充电台灯',1, '/api/images/goods/36.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (37,'收纳盒','生活用品',18.00,125,'桌面杂物收纳盒',5, '/api/images/goods/37.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (38,'洗脸毛巾','生活用品',9.90,330,'纯棉洗脸巾',7, '/api/images/goods/38.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (46,'镜子','生活用品',13.50,139,'宿舍粘贴化妆镜',3, '/api/images/goods/46.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (47,'被套床单三件套','生活用品',89.00,52,'宿舍床上套件',5, '/api/images/goods/47.svg');
INSERT IGNORE INTO `goods` (`id`,`name`,`category`,`price`,`stock`,`description`,`seller_id`,`image_url`) VALUES (49,'纸巾抽','生活用品',3.50,500,'抽取式面巾纸',2, '/api/images/goods/49.svg');
INSERT IGNORE INTO `orders` VALUES (4,'0ae08670f8664674b32942849da62461',5,2,10,'上海北京',120.00,0,'2026-10-07 19:16:38','2026-10-07 19:16:38',0);
INSERT IGNORE INTO `orders` VALUES (5,'003bfddc22e649bda066fe6fdb216a17',49,2,2,'上海北京',3.50,2,'2026-10-07 19:24:03','2026-10-07 19:24:03',0);
INSERT IGNORE INTO `orders` VALUES (6,'c3fbbfc7c028424bac929b38d4083cdf',47,2,5,'上海北京',89.00,0,'2026-10-07 19:25:10','2026-10-07 19:25:10',0);
INSERT IGNORE INTO `orders` VALUES (7,'ab72b0deeaa842078e14f69f88b4ccaa',2,1,7,'天庭南天门',25.50,0,'2026-10-11 21:49:44','2026-10-11 21:49:44',1);
INSERT IGNORE INTO `orders` VALUES (8,'6d7850b4f3c2489d9318d4e69986bc36',5,1,7,'天庭南天门',32.00,0,'2026-10-11 21:49:56','2026-10-11 21:49:56',1);
INSERT IGNORE INTO `orders` VALUES (9,'a21593a23b1a41b39c96b2697dd1c52d',9,1,2,'天庭南天门',36.00,1,'2026-10-11 21:49:58','2026-10-11 21:49:58',1);
INSERT IGNORE INTO `orders` VALUES (10,'ccaa2fcd8f8a49da9a7290980dc32f8e',15,1,5,'天庭南天门',35.00,0,'2026-10-11 21:50:01','2026-10-11 21:50:01',1);
INSERT IGNORE INTO `orders` VALUES (11,'528ad658f9de42b996e2c88d0e0516ec',19,1,5,'天庭南天门',1.00,0,'2026-10-11 21:50:04','2026-10-11 21:50:04',1);
INSERT IGNORE INTO `orders` VALUES (12,'3204ed64ede04d829f88fc1217a863f9',46,1,3,'天庭南天门',13.50,0,'2026-10-11 21:50:08','2026-10-11 21:50:08',1);
INSERT IGNORE INTO `student` VALUES (1,'张三','男',20,'天庭南天门','13900139000','2023-09-01','$2a$10$6vxVImCnx8v/KY31fZaPt.SnUeOCsO9AfyK7f2JhNvRS50VQpyJt6');
INSERT IGNORE INTO `student` VALUES (2,'李四','女',21,'三亚市','13700137000','2023-09-01','$2a$10$6vxVImCnx8v/KY31fZaPt.SnUeOCsO9AfyK7f2JhNvRS50VQpyJt6');
INSERT IGNORE INTO `student` VALUES (3,'王佳','女',20,'儋州市','13600136000','2023-09-01','$2a$10$6vxVImCnx8v/KY31fZaPt.SnUeOCsO9AfyK7f2JhNvRS50VQpyJt6');
INSERT IGNORE INTO `student` VALUES (4,'陈明','男',22,'琼海市','13500135000','2023-09-01','$2a$10$6vxVImCnx8v/KY31fZaPt.SnUeOCsO9AfyK7f2JhNvRS50VQpyJt6');
INSERT IGNORE INTO `student` VALUES (5,'刘婷','女',21,'文昌市','13400134000','2023-09-01','$2a$10$6vxVImCnx8v/KY31fZaPt.SnUeOCsO9AfyK7f2JhNvRS50VQpyJt6');
INSERT IGNORE INTO `student` VALUES (6,'赵宇','男',20,'东方市','13300133000','2023-09-01','$2a$10$6vxVImCnx8v/KY31fZaPt.SnUeOCsO9AfyK7f2JhNvRS50VQpyJt6');
INSERT IGNORE INTO `student` VALUES (7,'周欣','女',22,'澄迈县','13200132000','2023-09-01','$2a$10$6vxVImCnx8v/KY31fZaPt.SnUeOCsO9AfyK7f2JhNvRS50VQpyJt6');


-- ---------- 5. 自检 ----------
SELECT '初始化完成' AS 状态,
       (SELECT COUNT(*) FROM student) AS 学生数,
       (SELECT COUNT(*) FROM goods)   AS 商品数,
       (SELECT COUNT(*) FROM orders)  AS 订单数;

-- 演示账号：手机号 13900139000 ~ 13200132000（张三/李四/王佳/陈明/刘婷/赵宇/周欣）
-- 统一密码：123456