package com.sml.websockt.service;

import com.sml.websockt.pojo.GroupName;
import com.sml.websockt.pojo.Relation;
import com.sml.websockt.pojo.User;

import java.util.List;

public interface UserService {

    public int createUser(User user) ;

    public User findByLogin(User user) ;

    public User findByUserId(String userId);

    public List<User> findByCondition(User user);

    public List<User> findByCurUserList(User user);

    public List<GroupName> getFriendList(User user);

    public int addFriend(Relation relation);

    public User isFriend(Relation relation);

    public int delFriend(Relation relation);
}
