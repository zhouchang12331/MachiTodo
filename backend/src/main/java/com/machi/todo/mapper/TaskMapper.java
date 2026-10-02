package com.machi.todo.mapper;

import com.machi.todo.entity.Task;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 任务数据访问层（MyBatis 注解式 Mapper）。
 * SQL 直接写在注解里，省去 XML 配置文件；字段名与实体属性用 AS 对齐驼峰。
 */
public interface TaskMapper {

    /** 查询全部任务，最新的排在最前面 */
    @Select("SELECT id, content, is_done AS isDone FROM task ORDER BY id DESC")
    List<Task> selectAll();

    /**
     * 新增任务。
     * useGeneratedKeys + keyProperty：把数据库自增主键回填到入参对象的 id 上。
     */
    @Insert("INSERT INTO task(content, is_done) VALUES(#{content}, #{isDone})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Task task);

    /** 只更新完成状态，避免误改任务文本 */
    @Update("UPDATE task SET is_done = #{isDone} WHERE id = #{id}")
    int updateDone(Task task);

    /** 按 id 删除 */
    @Delete("DELETE FROM task WHERE id = #{id}")
    int deleteById(Long id);
}
