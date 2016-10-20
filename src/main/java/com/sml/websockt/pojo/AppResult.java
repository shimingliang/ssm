package com.sml.websockt.pojo;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.validation.BindingResult;

import java.io.Serializable;

/**
 * User: Administrator
 * Date: 16-10-9
 * Time: 下午9:59
 */
public class AppResult<T> implements Serializable{

    private String code; //返回的状态码 1 ok ,2 fail
    private String msg; //返回的消息
    private T data;//返回的数据
    private boolean success;//成功标识

    public AppResult() {
        this.success=false;
        this.msg="";
        this.code="";
    }

    public AppResult(String msg, boolean success) {
        this.msg = msg;
        this.success = success;
    }


    public void setSuccess()
    {
        this.success=true;
    }
    public void setFail(){
        this.success=false;
    }
    public void setSuccessMsg(String msg)
    {
        this.msg=msg;
        setSuccess();
    }
    public void setFailMsg(String msg){
        this.msg=msg;
        setFail();
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString(){
        return ToStringBuilder.reflectionToString(this);
    }

}