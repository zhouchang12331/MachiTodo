package com.machi.todo.service.impl;

import com.machi.todo.entity.Task;
import com.machi.todo.mapper.TaskMapper;
import com.machi.todo.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 任务业务实现类：这里只做参数兜底校验，具体 SQL 交给 Mapper。
 */
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;

    /** 构造器注入，比字段注入更利于测试 */
    public TaskServiceImpl(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    public List<Task> listAll() {
        return taskMapper.selectAll();
    }

    @Override
    public Task add(Task task) {
        // 任务文本为空直接拒绝，避免脏数据
        if (task == null || task.getContent() == null || task.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("任务内容不能为空");
        }
        task.setContent(task.getContent().trim());

        // 前端没传状态时，默认未完成
        if (task.getIsDone() == null) {
            task.setIsDone(false);
        }

        taskMapper.insert(task);
        return task;
    }

    @Override
    public boolean updateDone(Long id, Boolean isDone) {
        if (id == null || isDone == null) {
            throw new IllegalArgumentException("id 和 isDone 不能为空");
        }
        Task task = new Task();
        task.setId(id);
        task.setIsDone(isDone);
        return taskMapper.updateDone(task) > 0;
    }

    @Override
    public boolean remove(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id 不能为空");
        }
        return taskMapper.deleteById(id) > 0;
    }
}
