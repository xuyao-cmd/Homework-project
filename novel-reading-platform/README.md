# 基于 JavaWeb 的小说阅读平台

本项目按照《软件设计开发综合实训》考查题目完成，主题选择“起点中文网风格小说阅读平台”。

## 一、技术栈

### 前端
- Vue 2
- ElementUI
- Vue Router
- Axios

### 后端
- Spring Boot 2.7.18
- Spring Cloud 2021.0.9
- Eureka：服务注册与发现
- OpenFeign：服务间协作通信
- Spring Cloud Gateway：API 网关统一转发

## 二、项目结构

```text
novel-reading-platform
├── backend
│   ├── eureka-server   # 服务治理/注册中心，端口 8761
│   ├── api-gateway     # API 网关，端口 8080
│   ├── user-service    # 用户服务，端口 9001
│   └── novel-service   # 小说服务，端口 9002
├── frontend            # Vue + ElementUI 前端，端口 8081
└── docs                # 课程设计报告与接口说明
```

## 三、已实现功能

- 登录注册：内置 admin/123456、author/123456 两个测试账号，也支持注册新账号。
- 首页展示：小说列表、分类筛选、关键词搜索、热门数据展示。
- 小说详情：展示封面、作者、简介、标签、评分、点击量、章节目录。
- 章节阅读：支持章节正文阅读、字号放大缩小、返回目录。
- 小说发布：登录后发布小说，提交后首页和详情页可查看。
- 用户中心：展示用户信息，支持修改昵称、头像、简介。
- 微服务组件：Eureka 注册中心、Gateway 网关、Feign 服务调用。

## 四、在 IDEA 中运行

### 1. 导入后端

1. 打开 IntelliJ IDEA。
2. 选择 `Open`，打开 `backend/pom.xml`。
3. 等待 Maven 自动下载依赖。
4. 按顺序启动以下启动类：
   - `EurekaServerApplication`，访问 http://localhost:8761 查看注册中心。
   - `UserServiceApplication`。
   - `NovelServiceApplication`。
   - `GatewayApplication`。

### 2. 启动前端

在 IDEA Terminal 或系统终端中执行：

```bash
cd frontend
npm install
npm run serve
```

浏览器访问：

```text
http://localhost:8081
```

## 五、接口测试地址

启动所有后端服务后，可以通过 API 网关访问接口：

```text
GET  http://localhost:8080/api/novels
GET  http://localhost:8080/api/novels/1
GET  http://localhost:8080/api/novels/1/chapters
POST http://localhost:8080/api/users/login
POST http://localhost:8080/api/novels/publish
```

登录请求示例：

```json
{
  "username": "admin",
  "password": "123456"
}
```

## 六、注意事项

1. 本项目为课程设计演示版，默认使用内存数据，重启服务后发布内容会恢复初始状态。
2. 如果 8080、8081、8761、9001、9002 端口被占用，请修改对应模块的 `application.yml` 或 `vue.config.js`。
3. 前端请求统一走 `/api`，开发环境会代理到网关 `http://localhost:8080`。
4. 首次运行需要联网下载 Maven 和 npm 依赖。
