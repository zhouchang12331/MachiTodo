/**
 * いちごとカフェ · 小麻薯待办（MachiTodo）
 * ------------------------------------------------------------------
 * 数据来源：SpringBoot 后端（MyBatis + MySQL）。
 * 前端不再把任务存在内存数组 / localStorage 里当"数据库"用，
 * 每一次增、删、改、查都实实在在地请求后端接口。
 *
 * 后端接口约定：
 *   GET    /api/task/list          查询全部任务
 *   POST   /api/task/add           新增任务   body: { content, isDone }
 *   PUT    /api/task/update        更新状态   body: { id, isDone }
 *   DELETE /api/task/delete/{id}   删除任务
 */

/* ==================================================================
 * 一、后端地址配置
 * ================================================================== */

/**
 * 后端服务基础地址。
 * 后端默认跑在 localhost:8080，如需改动（换端口 / 换机器 / 换域名），
 * 只改这一行即可，例如：const API_BASE = "http://192.168.1.10:8080";
 */
const API_BASE = "http://localhost:8080";

/** 任务接口统一前缀，集中管理方便以后调整 */
const TASK_API = API_BASE + "/api/task";

/* ==================================================================
 * 二、极简 http 工具（基于原生 fetch，模仿 axios 的写法，无第三方库）
 * ================================================================== */

/**
 * 用法与 axios 基本一致：
 *   http.get(url)         -> Promise<{ data, status, response }>
 *   http.post(url, data)  -> Promise<...>
 *   http.put(url, data)   -> Promise<...>
 *   http.del(url)         -> Promise<...>
 * 返回值的 data 已经是解析好的 JSON（解析失败时回退成纯文本）。
 */
const http = {
  get(url, config) {
    return request("GET", url, undefined, config);
  },
  post(url, data, config) {
    return request("POST", url, data, config);
  },
  put(url, data, config) {
    return request("PUT", url, data, config);
  },
  del(url, config) {
    return request("DELETE", url, undefined, config);
  },
};

/**
 * 真正干活的请求函数：拼 header、发 fetch、解析响应、非 2xx 抛错。
 * @param {string} method HTTP 方法
 * @param {string} url    完整请求地址
 * @param {*} data        请求体数据（有值时自动 JSON 序列化）
 * @param {object} config 额外配置，目前只用到 headers
 */
async function request(method, url, data, config = {}) {
  const headers = { Accept: "application/json", ...(config.headers || {}) };
  const init = { method, headers };

  // 只有带请求体的方法才需要 Content-Type 和 body
  if (data !== undefined) {
    headers["Content-Type"] = "application/json";
    init.body = JSON.stringify(data);
  }

  const response = await fetch(url, init);
  const text = await response.text();

  // 后端删除成功可能返回空 body，所以先判空再解析
  let payload = null;
  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      payload = text;
    }
  }

  const result = { data: payload, status: response.status, response };

  // 非 2xx 视为失败，抛出错误交给上层统一提示
  if (!response.ok) {
    const error = new Error("请求失败 " + response.status);
    error.response = result;
    throw error;
  }
  return result;
}

/* ==================================================================
 * 三、任务相关接口封装（与后端 Controller 一一对应）
 * ================================================================== */

/** 查询全部任务：GET /api/task/list */
function apiGetTaskList() {
  return http.get(TASK_API + "/list");
}

/** 新增任务：POST /api/task/add，新任务默认未完成 */
function apiAddTask(content) {
  return http.post(TASK_API + "/add", { content: content, isDone: false });
}

/** 更新完成状态：PUT /api/task/update，只传 id 和 isDone */
function apiUpdateTask(id, isDone) {
  return http.put(TASK_API + "/update", { id: id, isDone: isDone });
}

/** 删除任务：DELETE /api/task/delete/{id} */
function apiDeleteTask(id) {
  return http.del(TASK_API + "/delete/" + id);
}

/**
 * 拆包裹：后端可能直接返回数组/对象，也可能返回 { code, msg, data } 统一结构。
 * 这里两种都兼容，调用方拿到的永远是真正的业务数据。
 */
function unwrapData(response) {
  const payload = response.data;

  // 统一响应结构：带 code 字段就按包裹处理
  if (payload && typeof payload === "object" && !Array.isArray(payload) && "code" in payload) {
    if (payload.code !== 200 && payload.code !== 0) {
      throw new Error(payload.msg || "后端返回异常");
    }
    return payload.data;
  }

  // 直接返回裸数据的情况
  return payload;
}

/* ==================================================================
 * 四、DOM 引用
 * ================================================================== */

const form = document.getElementById("todo-form");
const input = document.getElementById("todo-input");
const listEl = document.getElementById("todo-list");
const emptyHint = document.getElementById("empty-hint");
const countEl = document.getElementById("menu-count");
const loadingHint = document.getElementById("loading-hint");
const toastEl = document.getElementById("toast");

/* ==================================================================
 * 五、加载提示 & 错误提示
 * ================================================================== */

// 用计数而不是布尔值：多个请求同时进行时，最后一个结束才收起加载提示
let pendingCount = 0;

function showLoading() {
  pendingCount += 1;
  loadingHint.classList.remove("is-hidden");
  listEl.classList.add("is-loading");
}

function hideLoading() {
  pendingCount = Math.max(0, pendingCount - 1);
  if (pendingCount === 0) {
    loadingHint.classList.add("is-hidden");
    listEl.classList.remove("is-loading");
  }
}

// 提示条自动消失的定时器
let toastTimer = null;

/** 弹出错误/状态提示，3 秒后自动收起 */
function showToast(message) {
  toastEl.textContent = message;
  toastEl.classList.add("is-visible");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    toastEl.classList.remove("is-visible");
  }, 3000);
}

/** 把异常翻译成一句人话 */
function describeError(error) {
  // 没有 response 说明是 fetch 本身失败：后端没启动、断网、跨域被拦
  if (!error || !error.response) {
    return "连接不上后端服务，请确认 SpringBoot 已启动（" + API_BASE + "）";
  }
  return error.message || "网络异常，请稍后重试";
}

/* ==================================================================
 * 六、渲染（只负责把后端给的数据画到页面上）
 * ================================================================== */

/**
 * 根据任务数组渲染整个列表。
 * @param {Array<{id:number|string, content:string, isDone:boolean}>} tasks 后端返回的任务
 */
function renderList(tasks) {
  listEl.innerHTML = "";

  const list = Array.isArray(tasks) ? tasks : [];
  const remaining = list.filter((task) => !task.isDone).length;

  // 空列表：显示占位文案
  if (list.length === 0) {
    emptyHint.classList.remove("is-hidden");
    countEl.textContent = "还没有点单";
    return;
  }

  emptyHint.classList.add("is-hidden");
  countEl.textContent = remaining ? "未完成 " + remaining + " 杯" : "今日已清台";

  list.forEach((task) => {
    const li = document.createElement("li");
    li.className = "menu-item" + (task.isDone ? " is-done" : "");

    // ---- 勾选框 + 任务文本 ----
    const label = document.createElement("label");

    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.checked = !!task.isDone;
    if (task.isDone) checkbox.setAttribute("checked", "");
    checkbox.setAttribute("aria-label", "完成：" + task.content);

    // 勾选 -> 调后端更新状态 -> 重新拉一遍列表保证与数据库一致
    checkbox.addEventListener("change", async () => {
      try {
        await apiUpdateTask(task.id, checkbox.checked);
        await loadTasks();
      } catch (error) {
        checkbox.checked = !checkbox.checked; // 失败时把勾选状态还原
        showToast(describeError(error));
      }
    });

    const title = document.createElement("span");
    title.className = "title";
    title.textContent = task.content;

    label.append(checkbox, title);

    // ---- 删除按钮 ----
    const del = document.createElement("button");
    del.type = "button";
    del.className = "delete-btn";
    del.textContent = "删除";
    del.addEventListener("click", async () => {
      try {
        await apiDeleteTask(task.id);
        await loadTasks();
      } catch (error) {
        showToast(describeError(error));
      }
    });

    li.append(label, del);
    listEl.append(li);
  });
}

/* ==================================================================
 * 七、数据操作（每次操作都以后端为准）
 * ================================================================== */

/** 拉取列表并渲染，出错时弹提示并保留当前画面 */
async function loadTasks() {
  showLoading();
  try {
    const response = await apiGetTaskList();
    renderList(unwrapData(response));
  } catch (error) {
    showToast(describeError(error));
  } finally {
    hideLoading();
  }
}

/** 新增任务：成功后重新加载列表 */
async function createTask(content) {
  showLoading();
  try {
    await apiAddTask(content);
    await loadTasks(); // 重新拉列表，保证 id、排序都以数据库为准
    return true;
  } catch (error) {
    showToast(describeError(error));
    return false;
  } finally {
    hideLoading();
  }
}

/* ==================================================================
 * 八、事件绑定
 * ================================================================== */

// 提交表单 = 新增任务
form.addEventListener("submit", async (event) => {
  event.preventDefault();

  const content = input.value.trim();
  if (!content) return;

  input.value = ""; // 先清空输入框，避免重复提交
  const ok = await createTask(content);
  if (!ok) return; // 失败了就不抢焦点，方便用户重试
  input.focus();
});

/* ==================================================================
 * 九、首屏：页面加载自动拉取后端任务列表
 * ================================================================== */
loadTasks();
