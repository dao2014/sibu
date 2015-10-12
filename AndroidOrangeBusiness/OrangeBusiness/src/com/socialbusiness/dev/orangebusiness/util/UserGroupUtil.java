package com.socialbusiness.dev.orangebusiness.util;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.socialbusiness.dev.orangebusiness.api.APIManager;
import com.socialbusiness.dev.orangebusiness.api.RequestListResult;
import com.socialbusiness.dev.orangebusiness.api.RequestResult;
import com.socialbusiness.dev.orangebusiness.fragment.message.CustomConversationListFragment;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.Group;
import com.socialbusiness.dev.orangebusiness.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.demo.DemoContext;
import io.rong.imlib.RongIMClient;

/**
 * Created by zkboos on 2015/6/26.
 */
public class UserGroupUtil {

    public static CustomConversationListFragment customConversationListFragment;

    public static void getFriends(final Context context,CustomConversationListFragment conversationListFragment) {
        customConversationListFragment=conversationListFragment;
        APIManager.getInstance(context).listUsersByChat(null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                getGroups(context);
            }
        }, new Response.Listener<RequestResult<User.LeveledUsers>>() {
            @Override
            public void onResponse(RequestResult<User.LeveledUsers> response) {
//                Log.e("onResponse", "" + response);
                if (response.data != null)
                    processData(response.data);

//                getGroups(context);
            }
        });
    }

    public static void getFriends(final Context context) {
        APIManager.getInstance(context).listUsersByChat(null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                getGroups(context);
            }
        }, new Response.Listener<RequestResult<User.LeveledUsers>>() {
            @Override
            public void onResponse(RequestResult<User.LeveledUsers> response) {
//                Log.e("onResponse", "" + response);
                if (response.data != null)
                    processData(response.data);

//                getGroups(context);
            }
        });
    }

    private static void processData(User.LeveledUsers data) {

        final User.LeveledUsers leveldUsers = data;
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
        final List<User> allContacts = new ArrayList<User>();
        if (leveldUsers.up != null && !leveldUsers.up.isEmpty()) {
            allContacts.addAll(leveldUsers.up);
        }
        if (leveldUsers.up != null && !leveldUsers.center.isEmpty()) {
            allContacts.addAll(leveldUsers.center);
        }
        if (leveldUsers.up != null && !leveldUsers.down.isEmpty()) {
            allContacts.addAll(leveldUsers.down);
        }

//        Log.i("new group",""+leveldUsers.group);
        int size = allContacts.size();
        DemoContext.getInstance().userMap.clear();
        ArrayList<RongIMClient.UserInfo> infos = new ArrayList<RongIMClient.UserInfo>();
        for (int i = 0; i < size; i++) {
            User u = allContacts.get(i);
            RongIMClient.UserInfo info = new RongIMClient.UserInfo(MD5.encode(u.id), u.nickName, u.head);
            infos.add(info);
            if(RCloudContext.getInstance()!=null){
                RCloudContext.getInstance().getUserInfoCache().put(info.getUserId(),info);
            }
            DemoContext.getInstance().userMap.put(info.getUserId(),info);
        }

        User user= SettingsManager.getLoginUser();
        if(user!=null){
            RongIMClient.UserInfo info = new RongIMClient.UserInfo(MD5.encode(user.id), user.nickName, user.head);
            if(RCloudContext.getInstance()!=null){
                RCloudContext.getInstance().getUserInfoCache().put(info.getUserId(),info);
            }
            infos.add(info);
            DemoContext.getInstance().userMap.put(info.getUserId(),info);
        }
        DemoContext.getInstance().setFriends(infos);
        initRongyunGroup(leveldUsers.group);
//        Log.e("processData", "" + infos);
//            }
//        }).start();

    }

    public static  void getGroups(Context context) {
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        parameters.put("page", "1");
        parameters.put("pageSize", APIManager.MAX_PAGE_SIZE);
        APIManager.getInstance(context).listGroup(parameters, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }, new Response.Listener<RequestListResult<Group>>() {
            @Override
            public void onResponse(RequestListResult<Group> response) {
                initRongyunGroup(response.data);
            }
        });
    }

//    public static void initRongyunGroup(ArrayList<Group> data) {
//        if (data != null) {
//
//            int size = data.size();
//            HashMap<String, RongIMClient.Group> groupM = new HashMap<String, RongIMClient.Group>();
//
//            for (int i = 0; i < size; i++) {
//                Group g = data.get(i);
//                //群详id要统一要么都MD5要么都id
//                groupM.put(g.id, new RongIMClient.Group(g.id, g.name, null));
//                if(RCloudContext.getInstance()!=null){
//                    RCloudContext.getInstance().getGroupCache().put(g.id,groupM.get(g.id));
//                }
//            }
//            DemoContext.getInstance().setGroupMap(groupM);
//            if(customConversationListFragment!=null)
//            customConversationListFragment.refreshList();
////            Log.e("initRongyunGroup", "" + groupM);
//        } else {
//        }
//
//    }

    public static void initRongyunGroup(ArrayList<Group> data) {
        if(data!=null){

            int size=data.size();
            HashMap<String, RongIMClient.Group> groupM = new HashMap<String, RongIMClient.Group>();
            ArrayList<RongIMClient.Group> groups=new ArrayList<>();
            for(int i=0;i<size;i++){
                Group g=data.get(i);
                RongIMClient.Group group= new RongIMClient.Group(g.id,g.name,null);
                groupM.put(g.id,group);
                groups.add(group);
                if(RCloudContext.getInstance()!=null){
                    RCloudContext.getInstance().getGroupCache().put(g.id,groupM.get(g.id));
                    Log.i("====","g.id=="+g.id);
                }
            }
            DemoContext.getInstance().setGroupMap(groupM);
            DemoContext.getInstance().setGroupMap(groups);
            if(groups.size()!=0)
                RongIM.getInstance().syncGroup(groups,new RongIM.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        android.util.Log.e("=========","同步成功");
                    }

                    @Override
                    public void onError(ErrorCode errorCode) {

                    }
                });
            if(UserGroupUtil.customConversationListFragment!=null)
                UserGroupUtil.customConversationListFragment.refreshList();
        }else{
        }

    }
}
