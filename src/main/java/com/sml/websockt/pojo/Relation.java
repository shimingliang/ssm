package com.sml.websockt.pojo;

/**
 * User: shimingliang
 * Date: 16-9-27
 * Time: 上午10:02
 */
public class Relation {
    private Integer id;

    private String userId;

    private String friendId;

    private Integer groupId;


    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
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

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }
}
