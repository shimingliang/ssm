package com.sml.websockt.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sml.websockt.pojo.Content;
import com.sml.websockt.pojo.User;
import com.sml.websockt.service.ContentService;
import com.sml.websockt.service.UserService;
import com.sml.websockt.util.Constants;
import com.sml.websockt.util.GetHttpSessionConfigurator;
import com.sml.websockt.util.SessionUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
//该注解用来指定一个URI，客户端可以通过这个URI来连接到WebSocket。

/**
 * 类似Servlet的注解mapping。无需在web.xml中配置。
 * configurator = SpringConfigurator.class是为了使该类可以通过Spring注入。
 */

@ServerEndpoint(value = "/websocket", configurator = GetHttpSessionConfigurator.class)
public class MyWebSocket {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    public MyWebSocket() {
    }

    @Resource
    private ContentService contentService;
    @Resource
    private UserService userService;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    // 若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
//    private static CopyOnWriteArraySet<MyWebSocket> webSocketSet = new CopyOnWriteArraySet<MyWebSocket>();


    /**
     * 连接建立成功调用的方法
     *
     * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        User user = (User) httpSession.getAttribute(Constants.USER_INFO);
        SessionUtils.addSocket(user.getUserId() + "", session);   //加入set中
        addOnlineCount();           //在线数加1
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        try {

            List<String> keys = getKey(SessionUtils.clients, session);
            if (keys.size() > 0) {
                if (SessionUtils.hasConnection(keys.get(0))) {
                    SessionUtils.remove(keys.get(0));  //从set中删除
                } else {
                    System.out.println("该连接不存在，无法关闭！");
                }
            }
            subOnlineCount();           //在线数减1
            System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
            if (session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("来自客户端的消息:" + message);
        sendMessage(message, session);
    }

    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        try {
            System.out.println("发生错误");
            List<String> keys = getKey(SessionUtils.clients, session);
            if (keys.size() > 0) {
                if (SessionUtils.hasConnection(keys.get(0))) {
                    SessionUtils.remove(keys.get(0));  //从set中删除
                } else {
                    System.out.println("该连接不存在，无法关闭！");
                }
            }
            error.printStackTrace();
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message, Session session) {
        User u = new User();

        JSONObject jsonObject = JSON.parseObject(message);
        String id = jsonObject.get("id").toString();
        String mess = jsonObject.get("message").toString();
        String type = jsonObject.get("type").toString();
        System.out.println("来自客户端的内容:" + mess);
        String toUser = jsonObject.get("toUser").toString();
        jsonObject.put("ok", "1");

        //创建时间格式
        SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sm.format(new Date());

        //保存数据到数据库
        Content content = new Content();
        content.setContent(mess);
        content.setType(type);
        content.setCreateDate(date);

        try {
            if(Constants.MSG_TYPE_2.equals(type)){
                //入数据库
                content.setUserId(id);
                content.setUserName(u.getUsername());
                content.setToUser(toUser);
                if(!saveMessage(content)){
                    //返回给当前客户端提示
                    jsonObject.put("message", "添加好友失败，请重新添加！");
                    jsonObject.put("ok", "-2"); //添加好友失败返回状态码
                    session.getBasicRemote().sendText(jsonObject.toJSONString());
                    return;
                }
            }

            if (StringUtils.isBlank(toUser)) {
                System.out.println("接受者为空不能发送！,user:" + toUser);
                jsonObject.put("message", "接受者信息丢失，无法发送！");
                jsonObject.put("ok", "-1");
                session.getBasicRemote().sendText(jsonObject.toJSONString());
                return;
            }
            //该连接不在线
            if (!SessionUtils.hasConnection(toUser)) {
                //返回给当前客户端提示，该用户在不在线，无法发送
                jsonObject.put("message", "该用户已掉线，由于功能有限，无法发送！");
                jsonObject.put("ok", "-1");
                session.getBasicRemote().sendText(jsonObject.toJSONString());
                return;
            }
            if (StringUtils.isBlank(id) || id == "undefined") {
                jsonObject.put("message", "当前用户信息丢失，无法发送！");
                jsonObject.put("ok", "-1");
                session.getBasicRemote().sendText(jsonObject.toJSONString());
                return;
            }

            //查询当前用户信息
            u = userService.findByUserId(id);

            //入数据库
            content.setUserId(id);
            content.setUserName(u.getUsername());
            content.setToUser(toUser);
            if("3".equals(type)){
                content.setType("3");
            }else {
                content.setType("1");
            }
            if(!saveMessage(content)){
                //返回给当前客户端提示
                jsonObject.put("message", "保存聊天记录失败！");
                jsonObject.put("ok", "-1"); //添加好友失败返回状态码
                session.getBasicRemote().sendText(jsonObject.toJSONString());
            }

            //发送
            jsonObject.put("id", id);
            jsonObject.put("userName", u.getUsername());
            jsonObject.put("createDate", date);
            message = jsonObject.toJSONString();
            SessionUtils.getSession(toUser + "").getBasicRemote().sendText(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        MyWebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        MyWebSocket.onlineCount--;
    }

    /**
     * 保存消息到数据库
     *
     * @param content
     */
    public Boolean saveMessage(Content content) {
        try {
            contentService.insertSelective(content);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * map根据value得到key值
     *
     * @param map
     * @param session
     * @return
     */
    public List<String> getKey(Map map, Session session) {
        ArrayList all = new ArrayList();    //建一个数组用来存放符合条件的KEY值
        Set set = map.entrySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            if (entry.getValue().equals(session)) {
                all.add(entry.getKey().toString());
            }
        }
        return all;
    }
}