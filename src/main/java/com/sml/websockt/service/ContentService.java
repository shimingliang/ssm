package com.sml.websockt.service;

import com.sml.websockt.pojo.Content;
import com.sml.websockt.pojo.Relation;

import java.util.List;


public interface ContentService {
    List<Content> findContentList(Relation relation);

    List<Content> findMsgList(Content content);

    int insertSelective(Content content) ;
}
