package com.sml.websockt.service.impl;

import com.sml.websockt.dao.ContentDao;
import com.sml.websockt.dao.UserDao;
import com.sml.websockt.pojo.Content;
import com.sml.websockt.pojo.GroupName;
import com.sml.websockt.pojo.Relation;
import com.sml.websockt.pojo.User;
import com.sml.websockt.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Administrator on 2016/6/22.
 */
@Service("userService")
public class UserServiceImpl implements UserService {
    @Resource
    private UserDao userDao;
    @Resource
    private ContentDao contentDao;

    public int createUser(User user) {
        GroupName groupName = new GroupName();
        groupName.setGroupName("我的好友");
        groupName.setUserId(user.getUserId());
        userDao.insertGroup(groupName);
        return this.userDao.insert(user);
    }

    public User findByLogin(User user) {
        return userDao.findByLogin(user);
    }

    public User findByUserId(String userId) {
        return userDao.findByUserId(userId);
    }

    public List<User> findByCondition(User user) {
        return userDao.findByCondition(user);
    }

    public List<User> findByCurUserList(User user) {
        return userDao.findByCurUserList(user);
    }

    public List<GroupName> getFriendList(User user) {
        return userDao.getFriendList(user);
    }

    public int addFriend(Relation relation) {
        User user = new User();
        User user2 = new User();
        //查询当前的
        user.setUserId(relation.getUserId());
        GroupName groupName = userDao.queryGroupNameByUserId(user);
        //查询好友的
        user2.setUserId(relation.getFriendId());
        GroupName groupName2 = userDao.queryGroupNameByUserId(user2);

        Relation relation2 = new Relation();
        relation2.setUserId(relation.getFriendId());
        relation2.setFriendId(relation.getUserId());
        if(groupName != null && groupName2 != null){
            relation.setGroupId(groupName.getId());
            relation2.setGroupId(groupName2.getId());
        }else {
            return -1;
        }
        userDao.addFriend(relation2);
        //修改添加好友信息状态
        Content content = new Content();
        content.setType("3");
        content.setUserId(relation.getUserId());
        content.setToUser(relation.getFriendId());
        contentDao.editAddFriendMsgStatus(content);

        return userDao.addFriend(relation);
    }

    public User isFriend(Relation relation) {
        return userDao.isFriend(relation);
    }

    public int delFriend(Relation relation) {
        return userDao.delFriend(relation);
    }
}
