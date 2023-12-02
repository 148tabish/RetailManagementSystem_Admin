package com.retailmanagement.admin.adapters;

import com.retailmanagement.admin.models.Product;

import java.io.Serializable;
import java.util.ArrayList;

public class OrrderPojo implements Serializable {

    //Oid, Uid,counterid, date, status, Uname
    //Data[] - pid,pname,pimg,quantity,price,totprice
    private String Oid, Uid,counterid, date, status, Uname;

    public String getOid() {
        return Oid;
    }

    public void setOid(String oid) {
        Oid = oid;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getCounterid() {
        return counterid;
    }

    public void setCounterid(String counterid) {
        this.counterid = counterid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUname() {
        return Uname;
    }

    public void setUname(String uname) {
        Uname = uname;
    }

    private ArrayList<Product> productArrayList;

    public OrrderPojo(String oid, String uid, String counterid, String date, String status, String uname) {
        Oid = oid;
        Uid = uid;
        this.counterid = counterid;
        this.date = date;
        this.status = status;
        Uname = uname;
    }

    public ArrayList<Product> getProductArrayList() {
        return productArrayList;
    }

    public void setProductArrayList(ArrayList<Product> productArrayList) {
        this.productArrayList = productArrayList;
    }
}
