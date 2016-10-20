package com.sml.websockt.controller;

import com.sml.websockt.pojo.Content;
import com.sml.websockt.pojo.Relation;
import com.sml.websockt.service.ContentService;
import com.sml.websockt.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/")
public class IndexController {

    @Autowired
    private UserService userService;

    @Autowired
    private ContentService contentService;

    /**
     * 加载聊天记录
     *
     * @param relation
     * @return
     */
    @RequestMapping("loadContent")
    @ResponseBody
    public List<Content> loadContent(Relation relation) {
        List<Content> list = new ArrayList<Content>();
        try {
            if(StringUtils.isBlank(relation.getFriendId()) || StringUtils.isBlank(relation.getUserId())){
                return  list;
            }
            list = contentService.findContentList(relation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 加载添加好友消息
     * @param content
     * @return
     */
    @RequestMapping("loadMsg")
    @ResponseBody
    public List<Content> loadMsg(Content content) {
        List<Content> list = new ArrayList<Content>();
        try {
            if(StringUtils.isBlank(content.getUserId()) || StringUtils.isBlank(content.getType())){
                return  list;
            }
            list = contentService.findMsgList(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
