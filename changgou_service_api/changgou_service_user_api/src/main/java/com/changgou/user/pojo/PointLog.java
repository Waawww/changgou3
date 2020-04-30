package com.changgou.user.pojo;

import javax.persistence.Table;

/**
 * 积分日志表实体类
 */

@Table(name = "tb_point_log")
public class PointLog {
    private String orderId;
    private String userId;
    private Integer point;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }
}
