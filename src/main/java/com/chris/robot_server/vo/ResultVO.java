package com.chris.robot_server.vo;

import lombok.Data;

import java.io.Serializable;

/**
 *
 */
@Data
public class ResultVO<T> implements Serializable {

    private static final long serialVersionUID = -8630858531126402157L;

    /* 错误码 */
    private Integer status;

    /* 错误信息 */
    private String msg;

    /* 返回具体的内容 */
    private T data;
}
