package com.sml.websockt.pojo;

import java.util.List;

/**
 * User: shimingliang
 * Date: 16-9-27
 * Time: 下午3:39
 */
public class GroupName {

    private Integer id;

    private String userId;

    private String groupName;

    private List<User> userList;

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
