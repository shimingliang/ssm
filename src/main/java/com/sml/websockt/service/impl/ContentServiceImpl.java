package com.sml.websockt.service.impl;

import com.sml.websockt.dao.ContentDao;
import com.sml.websockt.pojo.Content;
import com.sml.websockt.pojo.Relation;
import com.sml.websockt.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("contentService")
public class ContentServiceImpl implements ContentService {

    @Autowired
    public ContentDao contentDao;

    @Override
    public List<Content> findContentList(Relation relation) {
        return contentDao.findContentList(relation);
    }

    @Override
    public List<Content> findMsgList(Content content){
        return contentDao.findMsgList(content);
    }

    @Override
    public int insertSelective(Content content) {
        return contentDao.insertSelective(content);
    }
}
