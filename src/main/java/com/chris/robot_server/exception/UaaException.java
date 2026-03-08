package com.chris.robot_server.exception;

import com.chris.robot_server.enums.ResultEnum;

/**
 *
 */
public class UaaException extends RuntimeException {
    private static final long serialVersionUID = -1271692266566010511L;

    //返回code
    private Integer code;

    public UaaException(String msg){
        super(msg);
        this.code = -1;
    }

    public UaaException(ResultEnum re){
        super(re.getMsg());
        this.code = re.getCode();
    }

    public UaaException(Integer code, String msg){
        super(msg);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
