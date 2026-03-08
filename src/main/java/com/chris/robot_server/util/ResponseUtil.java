package com.chris.robot_server.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ResponseUtil
 *
 * @author chris
 * 10/20/24
 */
public class ResponseUtil {
    public static void responseJson(HttpServletResponse response, int status, Object data) {
        try {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "*");
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(status);

            response.getWriter().write(JSONObject.toJSONString(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ResponseEntity<String> responseJson(int status, Object data) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        String jsonResponse = JSONObject.toJSONString(data);
        return new ResponseEntity<>(jsonResponse, headers, HttpStatus.valueOf(status));
    }

    // 也可以添加一个成功响应的快捷方法
    public static ResponseEntity<String> success(Object data) {
        return responseJson(HttpStatus.OK.value(), data);
    }
}
