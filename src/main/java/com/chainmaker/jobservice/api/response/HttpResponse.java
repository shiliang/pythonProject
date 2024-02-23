/**
 * @BelongsProject:chainmaker-mpc-service
 * @BelongsPackage:chainmaker-mpc-service
 * @Author: 王森
 * @CreateTime:2022-07-2022/7/19 18:35
 * @Description: 统一返回值：封装controller，统一返回指定格式数据
 * @Version: 1.0
 */

package com.chainmaker.jobservice.api.response;

import lombok.Data;
import org.springframework.http.HttpStatus;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class HttpResponse<T> implements Serializable {
    private String status;  //success,failed
    private int code; // 0 success;-1 failed
    private String message;
    private T data;

    public static <T> @NotNull HttpResponse<T> success(T data){
        HttpResponse response = new HttpResponse<>();
        response.setStatus("success");
        response.setCode(HttpStatus.OK.value());
        response.setData(data);
        return response;
    }

    public static <T> @NotNull HttpResponse<T> fail(int code, String msg){
        HttpResponse response = new HttpResponse<>();
        response.setStatus("failed");
        response.setCode(code);
        response.setMessage(msg);
        return response;
    }
}
