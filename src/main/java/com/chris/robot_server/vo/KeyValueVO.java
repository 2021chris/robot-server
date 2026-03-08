package com.chris.robot_server.vo;

import lombok.Data;

import java.io.Serializable;

/**
 */
@Data
public class KeyValueVO implements Serializable, Comparable<KeyValueVO> {

    private static final long serialVersionUID = -816031384346281093L;

    public KeyValueVO(){}

    public KeyValueVO(Object id, String name){
        this.id = id;
        this.name = name;
    }

    private Object id;

    private String name;

    @Override
    public int compareTo(KeyValueVO o) {
        //升序排序
        return Integer.parseInt(this.id.toString()) - Integer.parseInt(o.getId().toString());
    }
}
