package com.sml.websockt.controller;

import com.sml.websockt.pojo.AppResult;
import com.sml.websockt.pojo.GroupName;
import com.sml.websockt.pojo.Relation;
import com.sml.websockt.pojo.User;
import com.sml.websockt.service.UserService;
import com.sml.websockt.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("user")
public class UserController {

    private final static String CHARVIEW = "/WEB-INF/jsp/websocket/webim";
    private final static String ADDFRIENDVIEW = "/WEB-INF/jsp/websocket/add-friend";

    @Resource
    private UserService userService;

    @RequestMapping("turnToLogin")
    public String turnToLogin() {
        return "/login";
    }

    @RequestMapping("addFriendView")
    public ModelAndView addFriendView(HttpSession httpSession) {
        ModelAndView mv = new ModelAndView(ADDFRIENDVIEW);
        //把用户数据放入Session
        User u = (User)httpSession.getAttribute(Constants.USER_INFO);
        mv.addObject("user", u);
        return mv;
    }

    @RequestMapping("regist")
    public String turnToIndex() {
        return "/regist";
    }

    @RequestMapping("turnToUserList")
    public String turnToUserList() {
        return "user/userList";
    }

    /**
     * 跳转到聊天页面
     *
     * @return
     */
    @RequestMapping("chatView")
    public ModelAndView turnToWebSocketIndex(HttpSession httpSession) {
        ModelAndView view = new ModelAndView(CHARVIEW);
        //把用户数据放入Session
        User u = (User)httpSession.getAttribute(Constants.USER_INFO);
        view.addObject("user", u);
        return view;
    }

    /**
     * 登录
     * @param user
     * @param httpSession
     * @return
     */
    @RequestMapping("login")
    @ResponseBody
    public AppResult<User> queryUser(User user, HttpSession httpSession) {
        AppResult<User> appResult = new AppResult();
//        if (httpSession.getAttribute(Constants.USER_INFO) != null) {
//            //把用户数据放入Session
//            User u = (User)httpSession.getAttribute(Constants.USER_INFO);
//            if (user.getUserId().equals(u.getUserId())){
//                appResult.setData(u);
//                appResult.setSuccess();
//                return appResult;
//            }
//        }
        try {
            User u = userService.findByLogin(user);
            if (u != null) {
                //把用户数据放入Session
                httpSession.setAttribute(Constants.USER_INFO, u);
                appResult.setData(u);
                appResult.setSuccess();
                return appResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            appResult.setFail();
            appResult.setMsg("登录失败,请重新登录！");
            return appResult;
        }
        appResult.setFail();
        appResult.setMsg("用户名或密码错误!");
        return appResult;
    }

    /**
     * 获取好友列表
     * @param user
     * @return
     */
    @RequestMapping("getUserList")
    @ResponseBody
    public List<GroupName> getFriendList(User user) {
        List<GroupName> u = new ArrayList<GroupName>();
        try {
            if(StringUtils.isBlank(user.getUserId())){
                return  u;
            }
            u = userService.getFriendList(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return u;
    }

    /**
     * 查询是否是好友
     * @param relation
     * @return
     */
    @RequestMapping("isFriend")
    @ResponseBody
    public User isFriend(Relation relation,HttpSession httpSession) {
        User u = new User();
        try {
            if(StringUtils.isBlank(relation.getFriendId())){
                return  u;
            }
            //取当前登录用户Id
            User curUser  = (User)httpSession.getAttribute(Constants.USER_INFO);
            relation.setUserId(curUser.getUserId());
            User user = userService.isFriend(relation);
            if (user==null){
                return u;
            }
            u = user;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return u;
    }
    /**
     * 创建用户
     *
     * @param user
     * @throws IOException
     */
    @RequestMapping("create")
    @ResponseBody
    public  AppResult<User> createUser(User user, HttpServletResponse response){
        AppResult<User> appResult = new AppResult();
        try {
            if (StringUtils.isBlank(user.getUserId()) || StringUtils.isBlank(user.getPassword())) {
                appResult.setFail();
                appResult.setMsg("帐号或密码为空！");
                return appResult;
            }
            //该帐号已经存在
            if (userService.findByUserId(user.getUserId()) != null) {
                appResult.setFail();
                appResult.setMsg("该帐号已经存在！");
                return appResult;
            }
            userService.createUser(user);
            appResult.setSuccess();
            appResult.setData(user);
            return appResult;
        } catch (Exception e) {
            e.printStackTrace();
            appResult.setFail();
            appResult.setMsg("服务器异常，注册失败！");
            return appResult;
        }
    }

    /**
     * 搜索用户--添加好友时
     * @param user
     * @return
     */
    @RequestMapping("findUserList")
    @ResponseBody
    public List<User> findUserList(User user){
        List<User> userList = new ArrayList<User>();
        try {
            userList = userService.findByCurUserList(user);
        }catch (Exception e){
            e.printStackTrace();
        }
        return userList;
    }

    /**
     * 搜索用户--当前用户好友
     * @param user
     * @return
     */
    @RequestMapping("findCurUserList")
    @ResponseBody
    public List<User> findCurUserList(User user,HttpSession httpSession){
        //取当前登录用户Id
        User curUser  = (User)httpSession.getAttribute(Constants.USER_INFO);
        user.setUserId(curUser.getUserId());
        List<User> userList = new ArrayList<User>();
        try {
            userList = userService.findByCurUserList(user);
        }catch (Exception e){
            e.printStackTrace();
        }
        return userList;
    }

    /**
     * 添加好友
     * @param user
     * @param httpSession
     * @return
     */
    @RequestMapping("addFriend")
    @ResponseBody
    public String addFriend(User user,HttpSession httpSession){

        if(StringUtils.isBlank(user.getUserId())){
            return "failed";
        }
        try {
            //取当前登录用户Id
            User curUser  = (User)httpSession.getAttribute(Constants.USER_INFO);
            Relation relation = new Relation();
            relation.setUserId(curUser.getUserId());
            //好友ID
            relation.setFriendId(user.getUserId());
            if(userService.addFriend(relation) == 1){
                return "success";
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "failed";
    }

    /**
     * 删除好友
     * @param relation
     * @param httpSession
     * @return
     */
    @RequestMapping("delFriend")
    @ResponseBody
    public String delete(Relation relation,HttpSession httpSession){

        if(StringUtils.isBlank(relation.getFriendId())){
            return "failed";
        }
        try {
            //取当前登录用户Id
            User user  = (User)httpSession.getAttribute(Constants.USER_INFO);
            relation.setUserId(user.getUserId());
            if(userService.delFriend(relation) == 1){
                return "success";
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "failed";
    }

    @RequestMapping("loginOut")
    @ResponseBody
    public AppResult loginOut(HttpServletRequest request, HttpServletResponse response){
        HttpSession session = request.getSession();
        session.removeAttribute(Constants.USER_INFO);
        AppResult appResult = new AppResult();
        appResult.setSuccess();
        appResult.setMsg("注销成功！");
        return appResult;
    }


}
