# MachiTodo · 小麻薯待办 🍓

一个粉色系待办清单小应用：前端是纯静态的 HTML / CSS / JS（无任何第三方库），后端用 **SpringBoot + MyBatis + MySQL** 提供 `/api/task` 接口，数据真正落在数据库里。

页面保留了原来的「いちごとカフェ」风格：wobbly blob 背景、草莓粉 CTA 按钮、漂浮樱花动画，并且适配 `prefers-reduced-motion`。

---

## 目录结构

```
小麻薯清单/
├── index.html                 # 页面结构（菜单卡片、输入框、樱花层、提示条）
├── style.css                  # 全部样式：blob 背景 / 粉色 CTA / 樱花动画 / 减弱动态适配
├── main.js                    # 业务逻辑：fetch 版 http 工具 + 调后端接口 + 渲染
├── sql/
│   └── schema.sql             # 建库建表脚本（含示例数据）
└── backend/                   # SpringBoot 后端工程
    ├── pom.xml
    └── src/main/
        ├── java/com/machi/todo/
        │   ├── MachiTodoApplication.java        # 启动类
        │   ├── common/Result.java               # 统一响应结构
        │   ├── entity/Task.java                 # 实体类
        │   ├── mapper/TaskMapper.java           # MyBatis Mapper
        │   ├── service/TaskService.java         # Service 接口
        │   ├── service/impl/TaskServiceImpl.java# Service 实现
        │   ├── controller/TaskController.java   # 接口层
        │   └── config/
        │       ├── CorsConfig.java              # 全局跨域配置
        │       └── GlobalExceptionHandler.java  # 全局异常处理
        └── resources/
            ├── application.yml.example          # 配置模板（复制成 application.yml 后填写）
            └── application.yml                  # 本地实际配置，含密码，不入库
```

---

## 快速开始

### 1. 建库建表

用 Navicat、IDEA Database 或命令行执行 `sql/schema.sql`：

```bash
mysql -u root -p < sql/schema.sql
```

脚本会创建 `machi_todo` 库、`task` 表和三条示例数据。

### 2. 配置数据库连接

`application.yml` 含数据库账号密码，**没有提交到仓库**。先复制模板再改：

```bash
cd backend/src/main/resources
cp application.yml.example application.yml
```

然后编辑 `application.yml`，把自己的库名、用户名、密码填进去：

```yaml
spring:
  datasource:
    host: localhost        # ← 数据库地址
    port: 3306
    database: machi_todo   # ← 库名
    username: root         # ← 用户名
    password: 你的密码      # ← 密码
```

> `application.yml` 已在 `.gitignore` 里，改完不会被误提交。

### 3. 启动后端

需要 JDK 17 及以上：

```bash
cd backend
mvn spring-boot:run
```

或者先打包再启动：

```bash
mvn clean package
java -jar target/machi-todo-1.0.0.jar
```

启动后服务地址为 <http://localhost:8080>，控制台会打印执行的 SQL 方便调试。

### 4. 打开前端

直接用浏览器打开 `index.html`，或用 VS Code 的 Live Server。页面加载时会自动请求后端列表。

> **换后端地址**：只改 `main.js` 顶部的 `API_BASE` 常量即可，例如
> `const API_BASE = "http://192.168.1.10:8080";`

---

## 接口约定

所有接口统一返回：

```json
{ "code": 200, "msg": "操作成功", "data": {} }
```

| 方法 | 路径 | 请求体 / 参数 | 说明 |
| --- | --- | --- | --- |
| GET | `/api/task/list` | — | 查询全部任务（按 id 倒序） |
| POST | `/api/task/add` | `{ "content": "写周报", "isDone": false }` | 新增任务 |
| PUT | `/api/task/update` | `{ "id": 1, "isDone": true }` | 更新完成状态 |
| DELETE | `/api/task/delete/{id}` | 路径参数 id | 删除任务 |

返回的任务对象：

```json
{ "id": 1, "content": "给猫换水", "isDone": false }
```

已配置全局跨域（`/api/**`，允许 GET/POST/PUT/DELETE），前端跨端口访问不会被浏览器拦截。

---

## 前端行为说明

- **页面加载**：`http.get('/api/task/list')` 拉取列表并渲染，期间显示「正在从后厨端上来…」。
- **添加任务**：表单提交后 `http.post('/api/task/add')`，成功后重新拉取列表。
- **勾选任务**：`http.put('/api/task/update')` 只更新 `isDone`，之后重新拉取列表；失败会把勾选状态还原。
- **删除任务**：`http.del('/api/task/delete/{id}')`，成功后重新拉取列表。
- **异常提示**：请求失败时页面底部弹出提示条（后端没启动会明确提示检查 `API_BASE`）。

`main.js` 里的 `http` 工具模仿 axios 的调用形态（`get / post / put / del`），底层就是原生 `fetch`，方便以后无痛替换成真正的 axios。
