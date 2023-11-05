package com.crontiers.pillife.Model;

import java.io.Serializable;

/**
 * Created by Jaewoo on 2018-11-21.
 */
public class DataRank implements Serializable, MvConfig {

    private Integer rank;
    private double accuracy;
    private String image_link;
    private String pill_name;
    private String cls_name;
    private String href;
    private String code;
    private int id;

    public DataRank(){
        this.rank               = 0;
        this.accuracy           = 0f;
        this.image_link         = "";
        this.pill_name          = "";
        this.cls_name           = "";
        this.href               = "";
        this.code               = "";
        this.id                 = 0;


    }

    public DataRank(DataRank lv){
        this.rank               = lv.rank;
        this.accuracy           = lv.accuracy;
        this.image_link         = lv.image_link;
        this.pill_name          = lv.pill_name;
        this.cls_name           = lv.cls_name;
        this.href               = lv.href;
        this.code               = lv.code;
        this.id                 = lv.id;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public String getImage_link() {
        return image_link;
    }

    public void setImage_link(String image_link) {
        this.image_link = image_link;
    }

    public String getPill_name() {
        return pill_name;
    }

    public void setPill_name(String pill_name) {
        this.pill_name = pill_name;
    }

    public String getCls_name() {
        return cls_name;
    }

    public void setCls_name(String cls_name) {
        this.cls_name = cls_name;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}