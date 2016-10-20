package com.sml.websockt.dao;

import com.sml.websockt.pojo.GroupName;
import com.sml.websockt.pojo.Relation;
import com.sml.websockt.pojo.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDao {

    int insert(User record);

    int insertGroup(GroupName groupName);

    User findByLogin(User user);

    public  List<User> findByCondition(User user);

    public  List<User> findByCurUserList(User user);

    User findByUserId(String userId);

    public List<GroupName> getFriendList(User user);

    public int delFriend(Relation relation);

    public int addFriend(Relation relation);

    public GroupName queryGroupNameByUserId(User user);

    public User isFriend(Relation relation);


}
