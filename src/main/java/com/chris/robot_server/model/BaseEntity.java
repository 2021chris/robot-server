package com.chris.robot_server.model;

import java.io.Serializable;
import java.util.Date;

public class BaseEntity<ID extends Serializable> implements Serializable {

    private static final long serialVersionUID = -8672099562469061398L;
    private ID id;
    private Date createTime = new Date();
    private Date updateTime = new Date();

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
