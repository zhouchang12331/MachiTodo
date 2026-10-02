package com.machi.todo.common;

/**
 * 统一响应结构。
 * 前端收到的 JSON 形如：{ "code": 200, "msg": "操作成功", "data": ... }
 * code 为 200 表示业务成功，其它值表示失败（msg 里带原因）。
 *
 * @param <T> 业务数据类型
 */
public class Result<T> {

    /** 业务状态码：200 成功 */
    private Integer code;

    /** 提示信息 */
    private String msg;

    /** 业务数据，可能是对象、数组或 null */
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /** 成功并携带数据 */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /** 成功但不需要返回数据（新增/修改/删除） */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /** 业务失败 */
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
