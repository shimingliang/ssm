/*

 @Name: layui WebIM 1.0.0
 @Author：贤心
 @Date: 2014-04-25
 @Blog: http://sentsin.com
 
 */
var websocket = null;
var xxim = {};
;!function(win, undefined){

var config = {
    aniTime: 200,
    right: -232,
    api: {
        friend: $.sml_context.getRequestUrl()+'/user/getUserList', //好友列表接口
        group: '', //群组列表接口
        chatlog: '', //聊天记录接口
        groups: '', //群组成员接口
        sendurl: '' //发送消息接口
    },
    user: { //当前用户信息
        name: '',
        face: './../../resources/img/a1.jpg'
    },
    
    //自动回复内置文案，也可动态读取数据库配置
    autoReplay: [
        '您好，我现在有事不在，一会再和您联系。',
        '你没发错吧？',
    ],
    
    
    chating: {},
    hosts: (function(){
        var dk = location.href.match(/\:\d+/);
        dk = dk ? dk[0] : '';
        return 'http://' + document.domain + dk + '/';
    })(),
    json: function(url, data, callback, error){
        return $.ajax({
            type: 'POST',
            url: url,
            data: data,
            dataType: 'json',
            success: callback,
            error: error
        });
    },
    stopMP: function(e){
        e ? e.stopPropagation() : e.cancelBubble = true;
    }
}, dom = [$(window), $(document), $('html'), $('body')];

//发送消息
xxim.sendMessage = function(data){
    if(websocket.readyState != "1"){
        websocket = new WebSocket($.sml_context.getWSUrl());
        websocket.send(JSON.stringify(data));
    }else{
        websocket.send(JSON.stringify(data));
    }
};
xxim.initWebSocket = function(){
    //判断当前浏览器是否支持WebSocket
    if ('WebSocket' in window) {
        websocket = new WebSocket($.sml_context.getWSUrl());
    }
    else {
        toastr.warning("对不起！你的浏览器不支持webSocket");
    }

    //连接发生错误的回调方法
    websocket.onerror = function () {
//        setMessageInnerHTML("error");
    };

    //连接成功建立的回调方法
    websocket.onopen = function (event) {

    };

    //接收到消息的回调方法
    websocket.onmessage = function (event) {
        var json = JSON.parse(event.data);
        if(json.ok == "-1"){
            toastr.warning(json.message);
            return;
        }
        //有人同意添加好友
        if(json.type == "3"){
            toastr.info("您与"+json.userName+"已经是好友了");
            xxim.getDates(0);
            return;
        }
        //有人添加好友
        if(json.type == "2"){
            parent.layer.confirm(json.userName+'添加您为好友', {
                btn: ['同意','不同意'], //按钮
                shade: 0.8
            }, function(){
                layer.closeAll('dialog');
                //同意添加
                var url = $.sml_context.getRequestUrl()+"/user/addFriend";
                if(json.id == ''){
                    toastr.warning("添加失败！");
                    return;
                }
                var postData = {};
                postData.userId = json.id;
                $.sml_context.json(url,postData,function(data){
                    toastr.warning("添加成功！");
                    //发送到对方
                    var msgData = {};
                    msgData.id=$.sml_context.user.userId;
                    msgData.toUser= json.id;
                    msgData.message="[添加好友成功]";
                    msgData.type = "3";
                    xxim.sendMessage(msgData);
                    xxim.getDates(0);
                },function(data){
                    if(data.responseText == "failed" || data.status != "200"){
                        toastr.warning("添加失败！");
                    }else{
                        toastr.warning("添加成功！");
                        //发送到对方
                        var msgData = {};
                        msgData.id=$.sml_context.user.userId;
                        msgData.toUser= json.id;
                        msgData.message="[添加好友成功]";
                        msgData.type = "3";
                        xxim.sendMessage(msgData);
                        xxim.getDates(0);
                    }
                });

            }, function(){});
        }
        //判断窗口是否打开
        if($("#layim_chatmore #layim_userone"+json.id).length>0){
            //打开
            xxim.getServiseMsg(json);
        }else{
            //没打开
            $("#xxim_list li[data-id="+json.id+"] img").addClass("shake-opacity shake-constant");
        }
    };

    //连接关闭的回调方法
    websocket.onclose = function () {

    };

    //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，
    // 防止连接还没断开就关闭窗口，server端会抛异常。
    window.onbeforeunload = function (event) {
//        closeWebSocket();
        return "确定要退出吗？";
    };
    //关闭连接
    function closeWebSocket() {
        websocket.close();
    }
};

//主界面tab
xxim.tabs = function(index){
    var node = xxim.node;
    node.tabs.eq(index).addClass('xxim_tabnow').siblings().removeClass('xxim_tabnow');
    node.list.eq(index).show().siblings('.xxim_list').hide();
    if(node.list.eq(index).find('li').length === 0){
        xxim.getDates(index);
    }
};

//节点
xxim.renode = function(){
    var node = xxim.node = {
        tabs: $('#xxim_tabs>span'),
        list: $('.xxim_list'),
        online: $('.xxim_online'),
        setonline: $('.xxim_setonline'),
        onlinetex: $('#xxim_onlinetex'),
        xximon: $('#xxim_on'),
        layimFooter: $('#xxim_bottom'),
        xximHide: $('#xxim_hide'),
        xximSearch: $('#search'),
        searchMian: $('#xxim_searchmain'),
        closeSearch: $('#xxim_closesearch'),
        chatlist: $('.xxim_chatlist'),
        layimMin: $('#layim_min')
    }; 
};


//初始化窗口格局
xxim.layinit = function(){
    var node = xxim.node;
    
    //主界面
    try{
        /*
        if(!localStorage.layimState){       
            config.aniTime = 0;
            localStorage.layimState = 1;
        }
        */
        if(localStorage.layimState === '1'){
            xxim.layimNode.attr({state: 1}).css({right: config.right});
            node.xximon.addClass('xxim_off');
            node.layimFooter.addClass('xxim_expend').css({marginLeft: config.right});
            node.xximHide.addClass('xxim_show');
        }
    }catch(e){
        //layer.msg(e.message, 5, -1);
    }
};

//聊天窗口
xxim.popchat = function(param){
    var node = xxim.node, log = {};
    
    log.success = function(){

        xxim.chatbox = $('#layim_chatbox');
        log.chatlist = xxim.chatbox.find('.layim_chatmore>ul');
        log.header_name = xxim.chatbox.find('.header_name');
        log.header_name.html(param.name)

        log.chatlist.html('<li data-id="'+ param.id +'" type="'+ param.type +'"  id="layim_user'+ param.type + param.id +'"><span>'+ param.name +'</span><em>×</em></li>')
        xxim.tabchat(param, xxim.chatbox);
        //发送热键切换
        log.sendType = $('#layim_sendtype'), log.sendTypes = log.sendType.find('span');
        $('#layim_enter').on('click', function(e){
            config.stopMP(e);
            log.sendType.show();
        });
        log.sendTypes.on('click', function(){
            log.sendTypes.find('i').text('')
            $(this).find('i').text('√');
        });
        
        xxim.transmit();
    };


    //显示聊天窗口
    $("#layim_chatbox").show();
    //设置当前聊天信息
    $("#layim_area").attr("id","layim_area"+param.type + param.id);
    log.success();
};

//定位到某个聊天队列
xxim.tabchat = function(param){
    var node = xxim.node, log = {}, keys = param.type + param.id;
    xxim.nowchat = param;
    
    xxim.chatbox.find('#layim_user'+ keys).addClass('layim_chatnow').siblings().removeClass('layim_chatnow');
    xxim.chatbox.find('#layim_area'+ keys).addClass('layim_chatthis').siblings().removeClass('layim_chatthis');
    xxim.chatbox.find('#layim_group'+ keys).addClass('layim_groupthis').siblings().removeClass('layim_groupthis');
    
    xxim.chatbox.find('.layim_face>img').attr('src', param.face);
    xxim.chatbox.find('.layim_face, .layim_names').attr('href', param.href);
    xxim.chatbox.find('.layim_names').text(param.name);
    
//    xxim.chatbox.find('.layim_seechatlog').attr('href', config.chatlogurl + param.id);
    xxim.chatbox.find('#userId').val(param.id);

    log.groups = xxim.chatbox.find('.layim_groups');
    if(param.type === 'group'){
        log.groups.show();
    } else {
        log.groups.hide();
    }
    
//    $('#layim_write').focus();
    
};

//弹出聊天窗
xxim.popchatbox = function(othis){
    var node = xxim.node, dataId = othis.attr('data-id'), param = {
        id: dataId, //用户ID
        type: othis.attr('type'),
        name: othis.find('.xxim_onename').text(),  //用户名
        face: othis.find('.xxim_oneface').attr('src')  //用户头像
    }, key = param.type + dataId;
    //聊天窗口
    xxim.popchat(param);

    //去掉消息提醒
    $("#xxim_list li[data-id="+dataId+"] img").attr("class","xxim_oneface");
    $(".xxim_main").css("display","none");

    //设置聊天窗口的高度
    $(".layim_chatbox").css("height",$(window).height()+"px");
};

//请求群员
xxim.getGroups = function(param){
    var keys = param.type + param.id, str = '',
    groupss = xxim.chatbox.find('#layim_group'+ keys);
    groupss.addClass('loading');
    config.json(config.api.groups, {}, function(datas){
        if(datas.status === 1){
            var ii = 0, lens = datas.length;
            if(lens > 0){
                for(; ii < lens; ii++){
                    str += '<li data-id="'+ datas[ii].id +'" type="one"><img src="'+ datas[ii].face +'" class="xxim_oneface"><span class="xxim_onename">'+ datas[ii].name +'</span></li>';
                }
            } else {
                str = '<li class="layim_errors">没有群员</li>';
            }
            
        } else {
            str = '<li class="layim_errors">'+ datas.msg +'</li>';
        }
        groupss.removeClass('loading');
        groupss.html(str);
    }, function(){
        groupss.removeClass('loading');
        groupss.html('<li class="layim_errors">请求异常</li>');
    });
};

//消息传输
xxim.transmit = function(){
    var node = xxim.node, log = {};
    node.sendbtn = $('#layim_sendbtn');
    node.imwrite = $('#layim_write');

    //此处皆为模拟
    var keys = xxim.nowchat.type + xxim.nowchat.id;
    //聊天模版
    log.html = function(param, type){
        return '<li class="'+ (type === 'me' ? 'layim_chateme' : '') +'">'
            +'<div class="layim_chatuser">'
            + function(){
            if(type === 'me'){
                return '<span class="layim_chattime">'+ param.time +'</span>'
                    +'<span class="layim_chatname">'+ param.name +'</span>'
                    +'<img src="'+ param.face +'" >';
            } else {
                return '<img src="'+ param.face +'" >'
                    +'<span class="layim_chatname">'+ param.name +'</span>'
                    +'<span class="layim_chattime">'+ param.time +'</span>';
            }
        }()
            +'</div>'
            +'<div class="layim_chatsay">'+ param.content +'<em class="layim_zero"></em></div>'
            +'</li>';
    };

    log.imarea = xxim.chatbox.find('#layim_area'+ keys);
    //加载聊天数据
    var data = {};
    data.userId=$.sml_context.user.userId;
    data.friendId=xxim.nowchat.id;
    config.json($.sml_context.getRequestUrl()+"/loadContent",data,function(data){
        if(!data || data.length==0){
            return;
        }
        for(var i=0;i<data.length;i++){
            if(data[i].userId==$.sml_context.user.userId){
                log.imarea.append(log.html({
                    time: data[i].createDate,
                    name: data[i].userName,
                    face: config.user.face,
                    content:data[i].content
                }, 'me'));
            }else{
                log.imarea.append(log.html({
                    time: data[i].createDate,
                    name: data[i].userName,
                    face: config.user.face,
                    content:data[i].content
                }));
            }
            log.imarea.scrollTop(log.imarea[0].scrollHeight);
        }
    },function(data){})

    //发送
    log.send = function(){
        var data = {
            content: node.imwrite.html(),
            id: xxim.nowchat.id,
            sign_key: '', //密匙
            _: +new Date
        };

        //此处皆为模拟
        var keys = xxim.nowchat.type + xxim.nowchat.id;
        //聊天模版
        log.html = function(param, type){
            return '<li class="'+ (type === 'me' ? 'layim_chateme' : '') +'">'
                +'<div class="layim_chatuser">'
                + function(){
                if(type === 'me'){
                    return '<span class="layim_chattime">'+ param.time +'</span>'
                        +'<span class="layim_chatname">'+ param.name +'</span>'
                        +'<img src="'+ param.face +'" >';
                } else {
                    return '<img src="'+ param.face +'" >'
                        +'<span class="layim_chatname">'+ param.name +'</span>'
                        +'<span class="layim_chattime">'+ param.time +'</span>';
                }
            }()
                +'</div>'
                +'<div class="layim_chatsay">'+ param.content +'<em class="layim_zero"></em></div>'
                +'</li>';
        };

        log.imarea = xxim.chatbox.find('#layim_area'+ keys);

        if(data.content.replace(/\s/g, '') === ''){
//            toastr.info("说点啥呗！");
            node.imwrite.focus();
        } else {
            log.imarea.append(log.html({
                time: new Date().format("yyyy-MM-dd hh:mm:ss"),
                name: $.sml_context.user.name+' ['+$.sml_context.user.userId+']',
                face: config.user.face,
                content: data.content
            }, 'me'));
            node.imwrite.html('').focus();
            log.imarea.scrollTop(log.imarea[0].scrollHeight);

            //发送到对方
            var msgData = {};
            msgData.id=$.sml_context.user.userId;
            msgData.toUser=xxim.nowchat.id;
            msgData.message=data.content;
            msgData.type = "1";
            xxim.sendMessage(msgData);
        }
       
    };
    node.sendbtn.on('click', log.send);

    node.imwrite.keyup(function(e){
        if(e.keyCode === 13){
            log.send();
        }
    });
};

//接收服务器的消息
xxim.getServiseMsg = function(msg){
    //此处皆为模拟
    var keys = xxim.nowchat.type + xxim.nowchat.id;
    //聊天模版
    var html = function(param, type){
        return '<li class="'+ (type === 'me' ? 'layim_chateme' : '') +'">'
            +'<div class="layim_chatuser">'
            + function(){
            if(type === 'me'){
                return '<span class="layim_chattime">'+ param.time +'</span>'
                    +'<span class="layim_chatname">'+ param.name +'</span>'
                    +'<img src="'+ param.face +'" >';
            } else {
                return '<img src="'+ param.face +'" >'
                    +'<span class="layim_chatname">'+ param.name +'</span>'
                    +'<span class="layim_chattime">'+ param.time +'</span>';
            }
        }()
            +'</div>'
            +'<div class="layim_chatsay">'+ param.content +'<em class="layim_zero"></em></div>'
            +'</li>';
    };
    var imarea = xxim.chatbox.find('#layim_area'+ keys);
    imarea.append(html({
        time: msg.createDate,
        name: msg.userName,
        face: config.user.face,
        content:msg.message
    }));
    imarea.scrollTop(imarea[0].scrollHeight);
};
//事件
xxim.event = function(){
    var node = xxim.node;
    
    //主界面tab
    node.tabs.eq(0).addClass('xxim_tabnow');
    node.tabs.on('click', function(){
        var othis = $(this), index = othis.index();
        xxim.tabs(index);
    });
    
    //列表展收
    node.list.on('click', 'h5', function(){
        var othis = $(this), chat = othis.siblings('.xxim_chatlist'), parentss = othis.find("i");
        if(parentss.hasClass('fa-caret-down')){
            chat.hide();
            parentss.attr('class','fa fa-caret-right');
        } else {
            chat.show();
            parentss.attr('class','fa fa-caret-down');
        }
    });
    
    //设置在线隐身
    node.online.on('click', function(e){
        config.stopMP(e);
        node.setonline.show();
    });
    node.setonline.find('span').on('click', function(e){
        var index = $(this).index();
        config.stopMP(e);
        if(index === 0){
            node.onlinetex.html('在线');
            node.online.removeClass('xxim_offline');
        } else if(index === 1) {
            node.onlinetex.html('隐身');
            node.online.addClass('xxim_offline');
        }
        node.setonline.hide();
    });
    
//    node.xximon.on('click', xxim.expend);
//    node.xximHide.on('click', xxim.expend);
    
    //搜索
    node.xximSearch.keyup(function(){
        var uList = $(".user-list");
        var val = $(this).val().replace(/\s/g, '');
        if(val !== ''){
            //此处的搜索ajax参考xxim.getDates
            var url = $.sml_context.getRequestUrl()+"/user/findCurUserList";
            var  data = {};
            data.username = val;
            config.json(url,data,function(data){
                if(data.length==0){
                    uList.html('<span>无匹配到任何数据！</span>');
                    return;
                }
                var str = "";
                for(var j = 0; j < data.length; j++){
                    str += '<li data-id="'+ data[j].userId +'" class="xxim_childnode" type="'+ (0 === 0 ? 'one' : 'group') +'"><img src="./../../resources/img/a9.jpg" class="xxim_oneface"><span class="xxim_onename">'+ data[j].username +'</span></li>';
                }
                uList.html('<ul class="xxim_chatlist" style="display: block;">'+str+'</ul>');
            },function(data){
                uList.html('<span>请求异常！</span>');
            });
        } else {
            uList.html('');
        }
    });
    node.closeSearch.on('click', function(){
        $(this).hide();
        node.searchMian.hide();
        node.xximSearch.val('').focus();
        $("#xxim_list").show();
        $("#xxim_tabs").show();
    });
    
    //弹出聊天窗
    config.chatings = 0;
    node.list.on('click', '.xxim_childnode', function(){
        var othis = $(this);
        xxim.popchatbox(othis);
    });
    
    //点击最小化栏
    node.layimMin.on('click', function(){
        $(this).hide();
        $('#layim_chatbox').parents('.xubox_layer').show();
    });
    
    
    //document事件
    dom[1].on('click', function(){
        node.setonline.hide();
        $('#layim_sendtype').hide();
    });
};

//请求列表数据
xxim.getDates = function(index){
    if(index==0){
        config.api.friend = $.sml_context.getRequestUrl()+"/user/getUserList?userId="+$.sml_context.user.userId
    }
    var api = [config.api.friend, config.api.group, config.api.chatlog],
        node = xxim.node, myf = node.list.eq(index);
    myf.addClass('loading');
    config.json(api[index], {}, function(datas){
        var i = 0, myflen = datas.length, str = '', item;
        if(myflen > 0){
            if(index !== 2){
                for(; i < myflen; i++){
                    str += '<li data-id="'+ datas[i].id +'" class="xxim_parentnode">'
                        +'<h5><i class="fa fa-caret-right"></i><span class="xxim_parentname">'+ datas[i].groupName +'</span><em class="xxim_nums">（'+datas[i].userList.length+'）</em></h5>'
                        +'<ul class="xxim_chatlist">';
                    item = datas[i].userList;
                    for(var j = 0; j < item.length; j++){
                        str += '<li data-id="'+ item[j].userId +'" class="xxim_childnode" type="'+ (index === 0 ? 'one' : 'group') +'"><img src="./../../resources/img/a9.jpg" class="xxim_oneface"><span class="xxim_onename">'+ item[j].username +'</span></li>';
                    }
                    str += '</ul></li>';
                }
            } else {
                str += '<li class="xxim_liston">'
                    +'<ul class="xxim_chatlist">';
                for(; i < myflen; i++){
                    str += '<li data-id="'+ datas.data[i].id +'" class="xxim_childnode" type="one"><img src="'+ datas.data[i].face +'"  class="xxim_oneface"><span  class="xxim_onename">'+ datas.data[i].name +'</span><em class="xxim_time">'+ datas.data[i].time +'</em></li>';
                }
                str += '</ul></li>';
            }
            myf.html(str);
        } else {
            myf.html('<li class="xxim_errormsg">没有任何数据</li>');
        }
        myf.removeClass('loading');
    }, function(data){
        if(data.status==404){
            window.onbeforeunload = null;
            location.href="./../../login.html";
        }
        myf.html('<li class="xxim_errormsg">请求失败</li>');
        myf.removeClass('loading');
    });
};

//渲染骨架
xxim.view = function(){
    var xximNode = xxim.layimNode = $('<div id="xximmm" class="xxim_main">'
            +'<ul class="xxim_bottom" id="xxim_bottom">'
            +'<li class="xxim_online" id="xxim_online">'
            +'<i class="xxim_nowstate fa fa-check-circle"></i>'
            +'<span id="xxim_onlinetex">在线</span>'
            +'</li>'
            +'<li class="xxim_mymsg" id="xxim_add_friend" title="添加好友"><i class="fa fa-plus"></i><span class="xxim_logout">好友</span></li>'
            +'<li class="xxim_seter" id="xxim_seter" title="设置">'
            +'<i class="fa fa-gear"></i> <span class="xxim_logout">设置</span>'
            +'<div>'
            +'</div>'
            +'</li>'
            +'<li class="xxim_hide" id="xxim_hide"><i style="color: #b9c1c1"class="fa fa-times-circle"></i>'
            +'<span class="xxim_logout">退出</span></li>'
            +'<li id="xxim_on" class="xxim_icon xxim_on fa fa-ellipsis-v"></li>'
            +'<div class="layim_min" id="layim_min"></div>'
            +'</ul>'
            +'  <div class="xxim_search"><i class="fa fa-search"></i><input id="xxim_searchkey" placeholder="搜索" onfocus="this.blur()"><span id="xxim_closesearch">×</span></div>'
            +'  <div class="xxim_tabs" id="xxim_tabs"><span class="xxim_tabfriend" title="好友"><i class="fa fa-user"></i></span><span class="xxim_tabgroup" title="群组"><i class="fa fa-users"></i></span><span class="xxim_latechat"  title="最近聊天"><i class="fa fa-clock-o"></i></span></div>'
            +'  <ul class="xxim_list" id="xxim_list" style="display:block"></ul>'
            +'  <ul class="xxim_list"></ul>'
            +'  <ul class="xxim_list"></ul>'
            +'  <ul class="xxim_list xxim_searchmain" id="xxim_searchmain"></ul>'
    +'</div>');
    dom[3].append(xximNode);
    
    xxim.renode();
//    xxim.getDates(0);
    xxim.event();
    xxim.layinit();
    xxim.initWebSocket();
};
window.onload = function(){
    xxim.getDates(0);
};
$(function(){
    //注销
    var logOut = $('#xxim_hide');
    logOut.on("click",function(){
        window.onbeforeunload = null;
        var URL = $.sml_context.getRequestUrl()+"/user/loginOut";
        config.json(URL,"",function(data){
            if(data.success == true){
                history.back();
            }else{
                toastr.warning("注销失败，请重试！");
            }
        },function(data){
            toastr.warning("注销失败，请重试！");
        });
    });

    //添加好友
    var addFriend  = $("#xxim_add_friend");
    addFriend.on("click",function(){
        layer.open({
            type: 2,
            title: '添加好友',
            shadeClose: true,
            shade: 0.8,
            area: ['90%', '90%'],
            content: './../../jsp/websocket/add-friend.html?userId='+ $.sml_context.user.userId,
            end :function(){
                xxim.initWebSocket();
            }
        });
        $(".layui-layer-setwin a").attr("class","");
        $(".layui-layer-setwin a").append("<i style='font-size: 35px; margin-left: -20px;color: gray;' class='fa fa-times-circle'></i>")
    });
    //返回主页面
    var backHome = $("#layim_chatbox .header_back");
    backHome.on("click",function(){
        //显示主页面
        $(".xxim_main").show();
        //隐藏聊天窗口
        $("#layim_chatbox").hide();
        //隐藏搜索div
        $(".searchMeFriend").hide();
        //清空搜索框数据
        $(".searchMeFriend #search").val("");
        //清空查询列表
        $(".user-list").empty();
        //清空数据
        $("#layim_chatmore .layim_chatlist").empty();
        $("#layim_chatarea .layim_chatthis").empty();
        //重新设置ID
        $("#layim_chatarea .layim_chatthis").attr("id","layim_area");

    });

    //搜索好友
    var sFocus = $('.xxim_search');
    var searchMeFriend = $(".searchMeFriend");
    sFocus.on("click",function(){
        searchMeFriend.show();
        searchMeFriend.css("z-index","2");
        //隐藏主页面
        $("#xximmm").hide();
    });
    //查找自己好友取消
    $("#cancel").on("click",function(){
        searchMeFriend.hide();
        //显示主页面
        $("#xximmm").show();
        //清空搜索框数据
        $(".searchMeFriend #search").val("");
        //清空查询列表
        $(".user-list").empty();
    });
    //弹出聊天窗口
    $('.user-list').on('click', '.xxim_childnode', function(){
        var othis = $(this);
        //关闭手机软键盘
        $(".searchMeFriend .search").blur();
        setTimeout(function(){
            xxim.popchatbox(othis);
            searchMeFriend.hide();
        },500);
    });





//    //加载完成
    $(document).ready(function(){
        //请求信息
        var url = $.sml_context.getRequestUrl()+"/loadMsg";
        var postData = {};
        postData.userId=$.sml_context.user.userId;
        postData.type="2";
        $.sml_context.json(url,postData,function(data){
            if(data.length==0){
                return;
            }
            for(var i=0;i<data.length;i++){
                var k = i;
                parent.layer.confirm(data[i].userId+'添加您为好友', {
                    btn: ['同意','不同意'], //按钮
                    shade: 0.8
                }, function(){
                    layer.closeAll('dialog');
                    //同意添加
                    var url = $.sml_context.getRequestUrl()+"/user/addFriend";
                    if(data[k].userId == ''){
                        toastr.warning("添加失败！");
                        return;
                    }
                    var postData = {};
                    postData.userId = data[k].userId;
                    $.sml_context.json(url,postData,function(data){
                        toastr.warning("添加好友成功！");
                        //发送到对方
                        var msgData = {};
                        msgData.id=$.sml_context.user.userId;
                        msgData.toUser=data[k].userId;
                        msgData.message="[添加好友成功]";
                        msgData.type = "3";
                        xxim.sendMessage(msgData);
                        xxim.getDates(0);
                    },function(data){
                        if(data.responseText == "failed" || data.status != "200"){
                            toastr.warning("添加失败！");
                        }else{
                            toastr.warning("添加成功！");
                            //发送到对方
                            var msgData = {};
                            msgData.id=$.sml_context.user.userId;
                            msgData.toUser=data[k].userId;
                            msgData.message="[添加好友成功]";
                            msgData.type = "3";
                            xxim.sendMessage(msgData);
                            xxim.getDates(0);
                        }
                    });

                }, function(){});
            }
        },function(){
            toastr.warning("请求消息失败！")
        });
    });
});
}(window);