package com.sml.websockt.util;

import javax.websocket.Session;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能说明：用来存储业务定义的sessionId和连接的对应关系
 * 利用业务逻辑中组装的sessionId获取有效连接后进行后续操作
 */
public class SessionUtils {

    public static Map<String, Session> clients = new ConcurrentHashMap<String, Session>();

    /**
     *  获取 MyWebSocket对象
     * @param userId
     * @return
     */
    public static Session addSocket(String userId,Session session){
        clients.keySet();
        return clients.put(userId, session);
    }

    /**
     *  获取 MyWebSocket对象
     * @param userId
     * @return
     */
    public static Session getSession(String userId){
        return clients.get(userId);
    }

    /**
     * 移除 MyWebSocket对象
     * @param userId
     */
    public static void remove(String userId){
        clients.remove(userId);
    }

    /**
     * 判断是否有连接
     * @param userId
     * @return
     */
    public static boolean hasConnection(String userId) {
        return clients.containsKey(userId);
    }

    /**
     * 获取连接数
     * @return
     */
   public static Set<String> getCount(){
        return clients.keySet();
    }
}