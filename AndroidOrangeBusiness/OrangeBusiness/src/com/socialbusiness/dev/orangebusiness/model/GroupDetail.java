package com.socialbusiness.dev.orangebusiness.model;

import java.util.List;

/**
 * Created by zkboos on 2015/4/7.
 */
public class GroupDetail {
    public static final int OWNER = 1;
    public static final int AllowInvite = 1;
    public int isAllowInvite;
    public int isOwner;
    public List<GroupUser> users;
}
