(function($) {
	$.sml_context = {
            user : {}, //用户信息
			contextPath : null,// 系统上下文路径
			// 初始化系统上下文路径
			getContextPath : function() {
				if (!$.sml_context.contextPath) {
					var jsFileRelativePath = "/resources/js/";
					var scripts = document.getElementsByTagName("script");
					if (scripts) {
						for (var i = 0; i < scripts.length; i++) {
							var src = scripts[i].src;
							if (src && src.indexOf(jsFileRelativePath) != -1) {
								var pathArray = src.split(jsFileRelativePath);
								$.sml_context.contextPath = pathArray[0];
								break;
							}
						}
					} else {
						alert("JavaScript初始化异常，请开启浏览器的JavaScript脚本支持！");
					}
				}
				return $.sml_context.contextPath;
			},

            getRequestUrl : function(){
                return "http://119.29.4.240:8086/ssm";
            },

            getWSUrl : function(){
                return "ws://119.29.4.240:8086/ssm/websocket";
            },
            //ajax
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
            //ajax
            ajax: function(url,async, data, callback, error){
                return $.ajax({
                    type: 'POST',
                    url: url,
                    async : async,
                    data: data,
                    dataType: 'json',
                    success: callback,
                    error: error
                });
            },
            //获取地址栏参数
            getURLParam :function(paras){
                var url = decodeURI(location.href);
                var paraString = url.substring(url.indexOf("?")+1,url.length).split("&");
                var paraObj = {}
                for (i=0; j=paraString[i]; i++){
                    paraObj[j.substring(0,j.indexOf("=")).toLowerCase()] = j.substring(j.indexOf("=")+1,j.length);
                }
                var returnValue = paraObj[paras.toLowerCase()];
                if(typeof(returnValue)=="undefined"){
                    return "";
                }
                else{
                    return returnValue;
                }
            }
	}

})(jQuery);