package com.machi.todo.controller;

import com.machi.todo.common.Result;
import com.machi.todo.entity.Task;
import com.machi.todo.service.TaskService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 任务接口层，路径与前端 main.js 中的约定完全一致。
 *
 * GET    /api/task/list
 * POST   /api/task/add
 * PUT    /api/task/update
 * DELETE /api/task/delete/{id}
 */
@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /** 查询全部任务 */
    @GetMapping("/list")
    public Result<List<Task>> list() {
        return Result.success(taskService.listAll());
    }

    /**
     * 新增任务。
     * 请求体示例：{ "content": "写周报", "isDone": false }
     */
    @PostMapping("/add")
    public Result<Task> add(@RequestBody Task task) {
        return Result.success(taskService.add(task));
    }

    /**
     * 更新任务状态。
     * 请求体示例：{ "id": 1, "isDone": true }
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody Task task) {
        boolean ok = taskService.updateDone(task.getId(), task.getIsDone());
        return ok ? Result.success() : Result.error("任务不存在或更新失败");
    }

    /** 删除任务，id 走路径参数 */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        boolean ok = taskService.remove(id);
        return ok ? Result.success() : Result.error("任务不存在或删除失败");
    }
}
