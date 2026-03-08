package com.chris.robot_server.exception;

import com.chris.robot_server.enums.ResultEnum;

/**
 *
 */
public class TokenErrorException extends RuntimeException {
    private static final long serialVersionUID = -1271692266566010511L;

    //返回code
    private Integer code;

    public TokenErrorException(String msg){
        super(msg);
        this.code = -1;
    }

    public TokenErrorException(ResultEnum re){
        super(re.getMsg());
        this.code = re.getCode();
    }

    public TokenErrorException(Integer code, String msg){
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
