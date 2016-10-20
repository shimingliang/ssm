package com.sml.websockt.dao;

import com.sml.websockt.pojo.Content;
import com.sml.websockt.pojo.Relation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentDao {
    List<Content> findContentList(Relation relation);

    List<Content> findMsgList(Content content);

    int insertSelective(Content content) ;

    void editAddFriendMsgStatus(Content content);

}
