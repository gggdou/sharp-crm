package cn.itcast.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result {
    private Integer code;
    private String msg;
    private Object data;
    private static final Integer SUCCESS_CODE = 1;
    private static final Integer ERROR_CODE = 0;

    public static Result success() {
        return new Result(SUCCESS_CODE, "success",null );
    }

    public static Result success(Object data){
        return new Result(SUCCESS_CODE, "success", data);
    }

    public static Result success(String msg, Object data){
        return new Result(SUCCESS_CODE, msg, data);
    }

    public static Result error() {
        return new Result(ERROR_CODE, "error", null);
    }

    public static Result error(String msg) {
        return new Result(ERROR_CODE, msg, null);
    }
}
