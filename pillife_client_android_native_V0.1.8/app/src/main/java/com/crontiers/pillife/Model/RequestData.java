package com.crontiers.pillife.Model;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Callback;

/**
 * Created by Jaewoo on 2017-09-19.
 */
public class RequestData implements MvConfig {

    private String reqCall;
    private String reqTag;
    private Map<String, Object> reqParam;
    private Map<String, String> reqHeader;
    private Callback cbm;
    private REQUEST_TYPE reqType;
    private CONTENT_TYPE contentType;
    private boolean param;
    private int count;
    private int page;

    public RequestData(){
        this.reqCall = "";
        this.reqTag = "";
        this.reqParam = new HashMap<>();
        this.reqHeader = new HashMap<>();
        this.cbm = null;
        this.reqType = REQUEST_TYPE.POST;
        this.contentType = CONTENT_TYPE.JSON;
        this.param = true;
        this.count = MAX_ITEMS_PER_REQUEST;
        this.page = 0;
    }

    public String getReqCall() {
        return reqCall;
    }

    public void setReqCall(String reqCall) {
        this.reqCall = reqCall;
    }

    public String getReqTag() {
        return reqTag;
    }

    public void setReqTag(String reqTag) {
        this.reqTag = reqTag;
    }

    public Map<String, Object> getReqParam() {
        return reqParam;
    }

    public void setReqParam(Map<String, Object> reqParam) {
        this.reqParam = reqParam;
    }

    public Map<String, String> getReqHeader() {
        return reqHeader;
    }

    public void setReqHeader(Map<String, String> reqHeader) {
        this.reqHeader = reqHeader;
    }

    public Callback getCbm() {
        return cbm;
    }

    public void setCbm(Callback cbm) {
        this.cbm = cbm;
    }

    public REQUEST_TYPE getReqType() {
        return reqType;
    }

    public void setReqType(REQUEST_TYPE reqType) {
        this.reqType = reqType;
    }

    public boolean isParam() {
        return param;
    }

    public void setParam(boolean param) {
        this.param = param;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public CONTENT_TYPE getContentType() {
        return contentType;
    }

    public void setContentType(CONTENT_TYPE contentType) {
        this.contentType = contentType;
    }
}
