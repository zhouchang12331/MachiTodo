package com.machi.todo.service;

import com.machi.todo.entity.Task;

import java.util.List;

/**
 * 任务业务接口，Controller 只依赖这个接口，方便以后替换实现或加缓存。
 */
public interface TaskService {

    /** 查询全部任务 */
    List<Task> listAll();

    /** 新增任务，返回带自增 id 的任务对象 */
    Task add(Task task);

    /** 更新任务完成状态，返回是否更新成功 */
    boolean updateDone(Long id, Boolean isDone);

    /** 删除任务，返回是否删除成功 */
    boolean remove(Long id);
}
