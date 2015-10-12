package com.socialbusiness.dev.orangebusiness.model;

/**
 * 组群
 * Created by liangyaotian on 10/18/14.
 */
public class Group extends BaseModel {

    public String name;
    public String creatorId;
    public String summary;
    public int type;

    @Override
    public String toString() {
        return "Group{" +
                "name='" + name + '\'' +
                ", creatorId='" + creatorId + '\'' +
                ", summary='" + summary + '\'' +
                ", type=" + type +
                '}';
    }
}
