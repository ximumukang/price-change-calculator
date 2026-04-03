// 通用类所在包
package com.zang.pricechange.common;

// Lombok 注解：自动生成 getter、setter、toString 等方法
import lombok.Data;

/**
 * 统一响应结果类
 * 
 * 作用：所有接口都返回相同的 JSON 格式，方便前端统一处理
 * 格式示例：
 * {
 *   "code": 200,           // 状态码：200 表示成功，其他表示失败
 *   "message": "success",  // 提示信息
 *   "data": { ... }        // 实际返回的数据
 * }
 * 
 * @param <T> 泛型：data 字段的类型，可以是任意对象
 */
@Data  // Lombok 注解：自动生成所有字段的 getter 和 setter 方法
public class Result<T> {
    // 状态码：200 表示成功，400 表示客户端错误，500 表示服务器错误
    private int code;
    // 提示信息：成功时返回 "success"，失败时返回错误原因
    private String message;
    // 实际数据：接口返回的具体内容（如用户信息、列表等）
    private T data;

    /**
     * 创建成功响应（带数据）
     * @param data 要返回的数据
     * @return 封装好的 Result 对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 创建成功响应（不带数据）
     * 用于删除等操作，只需要告诉前端成功即可
     * @return 封装好的 Result 对象
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 创建失败响应
     * @param code 错误状态码
     * @param message 错误提示信息
     * @return 封装好的 Result 对象
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
