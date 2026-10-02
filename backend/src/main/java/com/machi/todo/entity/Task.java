package com.machi.todo.entity;

/**
 * 任务实体，对应数据库表 task。
 * 字段与前端约定的 JSON 保持一致：id / content / isDone
 */
public class Task {

    /** 主键，自增 */
    private Long id;

    /** 任务文本 */
    private String content;

    /**
     * 是否已完成，对应数据库列 is_done（MySQL 里用 0/1 表示）。
     * 这里刻意不写成 boolean isDone，避免 Jackson 序列化字段名变成 "done"。
     */
    private Boolean isDone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsDone() {
        return isDone;
    }

    public void setIsDone(Boolean isDone) {
        this.isDone = isDone;
    }

    @Override
    public String toString() {
        return "Task{id=" + id + ", content='" + content + "', isDone=" + isDone + "}";
    }
}
