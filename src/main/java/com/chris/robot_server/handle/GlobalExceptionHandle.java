package com.chris.robot_server.handle;

import com.chris.robot_server.exception.TokenErrorException;
import com.chris.robot_server.exception.UaaException;
import com.chris.robot_server.util.ResultUtil;
import com.chris.robot_server.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

/**
 * @author Chris
 * @date 9/21/21 2:09 PM
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandle {

    @ExceptionHandler(value = TokenErrorException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ResultVO<Object> handleTokenErrorException(TokenErrorException ze){
        log.warn("{}", ze.getMessage());
        return ResultUtil.error(ze.getCode(), ze.getMessage());
    }

    @ExceptionHandler(value = UaaException.class)
    @ResponseBody
    public ResultVO<Object> handle(UaaException ze){
        log.warn("{}", ze.getMessage());
        return ResultUtil.error(ze.getCode(), ze.getMessage());
    }

    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public ResultVO<Object> handle(BindException ze){
        log.warn("{}", ze.getAllErrors());
        return ResultUtil.error(Objects.requireNonNull(ze.getBindingResult().getFieldError()).getDefaultMessage());
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResultVO<Object> handle(Exception e){
        e.printStackTrace();
        log.error("{}", e.getMessage());
        return ResultUtil.error(-1, e.getMessage());
    }

    @ExceptionHandler(value = RuntimeException.class)
    @ResponseBody
    public ResultVO<Object> handleRuntimeException(RuntimeException e){
        e.printStackTrace();
        log.error("{}", e.getMessage());
        return ResultUtil.error(-1, e.getMessage());
    }

}
