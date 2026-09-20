# 多模态财报深度解析平台

## 项目简介

多模态财报深度解析平台是一个专业的财务数据分析与风险评估系统，采用前后端分离架构，提供财报分析、公司画像、风险分析、论坛中心等功能。

## 技术栈

### 后端技术栈
- Spring Boot 2.7.14（核心框架）
- Spring MVC（接口开发）
- Spring Security + JWT（权限认证）
- MyBatis Plus 3.5.3.1（数据库操作）
- Maven（项目构建与依赖管理）
- MySQL 8.0（数据库）

### 前端技术栈
- Vue 3.3.4（前端框架）
- Vue Router 4.2.4（路由管理）
- Axios 1.5.0（HTTP请求）
- Element Plus 2.3.14（UI框架）
- Pinia 2.1.6（状态管理）
- Vite 4.4.9（构建工具）

## 项目结构

```
finance/
├── backend/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/finance/
│   │   │   │   ├── config/        # 配置类
│   │   │   │   ├── controller/    # 控制器
│   │   │   │   ├── dto/           # 数据传输对象
│   │   │   │   ├── entity/        # 实体类
│   │   │   │   ├── mapper/        # 数据访问层
│   │   │   │   ├── security/      # 安全配置
│   │   │   │   ├── service/       # 业务逻辑层
│   │   │   │   └── util/          # 工具类
│   │   │   └── resources/
│   │   │       ├── application.properties  # 配置文件
│   │   │       └── schema.sql              # 数据库脚本
│   │   └── test/
│   └── pom.xml
└── frontend/               # 前端项目
    ├── src/
    │   ├── api/           # API接口
    │   ├── components/    # 组件
    │   ├── layouts/       # 布局
    │   ├── router/        # 路由
    │   ├── stores/        # 状态管理
    │   ├── utils/         # 工具类
    │   ├── views/         # 页面
    │   ├── App.vue
    │   └── main.js
    ├── index.html
    ├── package.json
    └── vite.config.js
```

## 数据库配置

### 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS finance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 导入数据表

执行 `backend/src/main/resources/schema.sql` 脚本创建用户表。

### 数据库连接配置

在 `backend/src/main/resources/application.properties` 中配置数据库连接：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/finance?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
spring.datasource.username=root
spring.datasource.password=your_password
```

## 后端启动

### 前置要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+

### 启动步骤

1. 进入后端目录：
```bash
cd backend
```

2. 编译项目：
```bash
mvn clean install
```

3. 运行项目：
```bash
mvn spring-boot:run
```

或者直接运行主类：
```bash
java -jar target/finance-system-1.0.0.jar
```

4. 访问地址：http://localhost:8080/api

## 前端启动

### 前置要求
- Node.js 16+
- npm 或 yarn

### 启动步骤

1. 进入前端目录：
```bash
cd frontend
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

4. 访问地址：http://localhost:3000

### 构建生产版本

```bash
npm run build
```

## 功能模块

### 1. 用户认证
- 用户注册（用户名、邮箱、手机号唯一性校验）
- 用户登录（支持用户名/邮箱/手机号登录）
- JWT token认证
- 密码加密存储

### 2. 财报分析
- 公司搜索
- 财务指标分析
- 盈利能力分析
- 偿债能力分析
- 营运能力分析
- 成长能力分析

### 3. 公司画像
- 公司基本信息查询
- 经营信息展示
- 联系信息展示

### 4. 风险分析
- 综合风险评级
- 风险详情分析
- 风险建议提供

### 5. 论坛中心
- 帖子发布
- 帖子浏览
- 热门帖子推荐
- 分类浏览

### 6. 个人中心
- 基本信息管理
- 密码修改
- 分析历史查看
- 收藏管理

### 7. AI财报分析
- AI对话分析财报数据
- Qwen模型调用
- 动态进度条显示分析过程
- 分析结果格式化展示

### 8. 风险评估
- XGBoost风险评估模型
- 综合风险评级
- 风险概率计算
- 风险建议提供

### 9. 管理员功能
- 用户管理（禁用/启用用户）
- 财报管理
- 帖子管理（含评论管理）
- 主理人管理

## API接口

### 认证接口

#### 用户注册
- POST `/api/auth/register`
- 请求体：
```json
{
  "username": "用户名",
  "email": "邮箱",
  "phone": "手机号",
  "password": "密码",
  "confirmPassword": "确认密码"
}
```

#### 用户登录
- POST `/api/auth/login`
- 请求体：
```json
{
  "loginName": "用户名/邮箱/手机号",
  "password": "密码",
  "captcha": "验证码",
  "uuid": "验证码UUID"
}
```
- 响应：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "JWT令牌",
    "userId": 1,
    "username": "用户名",
    "email": "邮箱",
    "phone": "手机号"
  }
}
```

#### 修改密码
- POST `/api/auth/change-password`
- 请求头：`Authorization: Bearer {token}`
- 请求体：
```json
{
  "userId": 1,
  "oldPassword": "原密码",
  "newPassword": "新密码"
}
```
- 响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "密码修改成功"
}
```

### 风险评估接口

#### 获取风险评估
- GET `/api/risk/{stockCode}`
- 路径参数：`stockCode` - 股票代码
- 响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "riskProb": 25.5,
    "riskLevel": "低风险",
    "companyName": "公司名称"
  }
}
```

### AI分析接口

#### AI对话分析
- POST `/api/ai/chat`
- 请求头：`Authorization: Bearer {token}`
- 请求体：
```json
{
  "stockCode": "股票代码",
  "reportYear": "报告年份",
  "question": "用户问题"
}
```
- 响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "response": "AI回答内容"
  }
}
```

## 默认测试账号

- 用户名：admin
- 密码：123456
- 用户名：冯
- 密码：123456

## 注意事项

1. 确保MySQL服务已启动
2. 修改数据库连接密码为实际密码
3. JWT密钥建议在生产环境中修改
4. 前端开发环境已配置代理，无需处理跨域问题

## 开发规范

### 后端开发规范
- 遵循RESTful API设计规范
- 统一使用ApiResponse封装响应结果
- 使用Lombok简化代码
- 异常处理统一在Controller层

### 前端开发规范
- 使用Vue 3 Composition API
- 统一使用Element Plus组件库
- API请求统一封装
- 使用Pinia进行状态管理

## 许可证

MIT License