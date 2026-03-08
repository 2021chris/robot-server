package com.chris.robot_server.util;

import com.github.pagehelper.Page;
import com.chris.robot_server.enums.ResultEnum;
import com.chris.robot_server.vo.PageResultVO;
import com.chris.robot_server.vo.ResultVO;

import java.util.HashMap;

/**
 *
 */
public class ResultUtil {

    public static ResultVO<Object> success(Object object){
        ResultVO<Object> result = new ResultVO<Object>();
        result.setStatus(0);
        result.setMsg("Success");
        result.setData(object);
        return result;
    }

    public static ResultVO<Object> success(){
        return success(null);
    }

    public static ResultVO<Object> error(ResultEnum resultEnum){
        ResultVO<Object> result = new ResultVO<Object>();
        result.setStatus(-1);
        result.setMsg(resultEnum.getMsg());
        return result;
    }

    public static ResultVO<Object> error(String msg){
        ResultVO<Object> result = new ResultVO<Object>();
        result.setStatus(-1);
        result.setMsg(msg);
        return result;
    }

    public static ResultVO<Object> error(Integer code, String msg){
        ResultVO<Object> result = new ResultVO<Object>();
        result.setStatus(code);
        result.setMsg(msg);
        return result;
    }

    public static ResultVO<Object> success(Object pageObjcet, Object list){
        PageResultVO<Object> pageResultVO = new PageResultVO<Object>();
        pageResultVO.setPages(((Page)pageObjcet).getPages());
        pageResultVO.setCurrent(((Page)pageObjcet).getPageNum());
        pageResultVO.setTotal(((Page)pageObjcet).getTotal());
        pageResultVO.setList(list);
        return success(pageResultVO);
    }

    public static ResultVO<Object> sendPhoneCmd(Integer code, Object object){
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("data", object);
        return success(map);
    }
}
