package com.socialbusiness.dev.orangebusiness.model;

/**
 * 消息基类
 * Created by liangyaotian on 10/18/14.
 */
public class Message extends BaseModel {
    public String createTime;
    public User from;
    public User to;
    public String content;
}
