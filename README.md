<<<<<<< .mine
# BigEvent - 现代化博客管理系统

一个基于 Spring Boot + Vue 3 的现代化博客管理系统，支持文章管理、分类管理、留言互动、相册等功能。

## ✨ 功能特性

- 📝 **文章管理** - 支持富文本编辑、Markdown、文章分类
- 🏷️ **分类管理** - 灵活的文章分类系统
- 💬 **留言互动** - 实时留言和点赞功能
- 📸 **相册管理** - 图片上传和管理
- 👤 **用户管理** - 用户注册、登录、权限管理
- 📊 **数据统计** - 访问量统计、热门文章
- 🎨 **现代化UI** - 响应式设计，支持暗色模式

## 🛠️ 技术栈

### 后端
- **Spring Boot 3.1.5** - Java后端框架
- **MyBatis** - 持久层框架
- **Redis** - 缓存和Session存储
- **JWT** - 无状态认证
- **BCrypt** - 密码加密
- **MySQL** - 数据库

### 前端
- **Vue 3** - 渐进式JavaScript框架
- **Element Plus** - UI组件库
- **Vue Router** - 路由管理
- **Pinia** - 状态管理
- **Axios** - HTTP客户端
- **Vite** - 构建工具

## 📦 项目结构

```
BigEvent-main/
├── big-event-backend/          # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/           # Java源码
│   │   │   └── resources/      # 配置文件
│   │   └── test/               # 测试代码
│   └── pom.xml                 # Maven配置
├── big-event-frontend/         # 前端项目（管理后台）
│   ├── src/
│   │   ├── api/               # API接口
│   │   ├── components/         # 组件
│   │   ├── views/             # 页面
│   │   ├── router/            # 路由
│   │   └── stores/            # 状态管理
│   └── package.json
└── README.md
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 后端启动

1. **克隆项目**
```bash
git clone https://github.com/yourusername/big-event.git
cd big-event/big-event-backend
```

2. **配置数据库**
```sql
CREATE DATABASE big_event CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **配置环境变量**
```bash
# 复制环境变量示例文件
cp .env.example .env

# 编辑 .env 文件，填入实际配置
# 必须配置：
# - DB_PASSWORD: 数据库密码
# - JWT_SECRET: JWT密钥（至少32位）
```

4. **生成JWT密钥**
```bash
# Linux/Mac
openssl rand -base64 32

# Windows PowerShell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

5. **配置application.yml**
```yaml
# 在 application-dev.yml 中配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/big_event
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:your_password}
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

jwt:
  secret: ${JWT_SECRET:your_jwt_secret}
```

6. **启动后端**
```bash
mvn spring-boot:run
# 或
java -jar target/big-event-0.0.1-SNAPSHOT.jar
```

后端服务将在 `http://localhost:8081` 启动

### 前端启动

1. **进入前端目录**
```bash
cd big-event-frontend
```

2. **安装依赖**
```bash
npm install
# 或
yarn install
```

3. **配置API地址**
```javascript
// src/utils/request.js
const instance = axios.create({
  baseURL: '/api',  // 开发环境使用代理
  // baseURL: 'http://localhost:8081/api',  // 生产环境
  timeout: 15000
});
```

4. **启动开发服务器**
```bash
npm run dev
# 或
yarn dev
```

前端服务将在 `http://localhost:5173` 启动

## 🔒 安全配置

### 重要安全设置

1. **JWT密钥** - 必须使用强密钥（至少32位）
2. **数据库密码** - 使用环境变量，不要硬编码
3. **CORS配置** - 生产环境限制允许的域名
4. **速率限制** - 登录接口已实现速率限制（5次/分钟）

### 生产环境部署检查清单

- [ ] 设置 `JWT_SECRET` 环境变量（至少32位强密钥）
- [ ] 配置数据库密码（使用环境变量）
- [ ] 更新CORS配置中的实际域名
- [ ] 禁用调试日志
- [ ] 配置HTTPS
- [ ] 设置防火墙规则

## 📝 API文档

### 认证接口

- `POST /user/register` - 用户注册
- `POST /user/login` - 用户登录
- `POST /user/refresh` - 刷新Token
- `GET /user/userInfo` - 获取用户信息

### 文章接口

- `GET /article` - 获取文章列表
- `GET /article/detail` - 获取文章详情
- `POST /article` - 创建文章
- `PUT /article` - 更新文章
- `DELETE /article` - 删除文章

更多API文档请参考代码中的Controller类。

## 🧪 测试

```bash
# 后端测试
cd big-event-backend
mvn test

# 前端测试
cd big-event-frontend
npm run test
```

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📞 联系方式

如有问题，请提交 Issue 或联系项目维护者。

## 🙏 致谢

感谢所有为本项目做出贡献的开发者！

---

**注意：** 首次部署时，所有现有Token将失效，用户需要重新登录。

=======
# SpringBoot
这是一个基于springboot+vue3+mabaits-plus的项目，这个是基于big-event的二次开发































































































































































































































>>>>>>> .theirs
