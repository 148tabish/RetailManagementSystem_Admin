package com.retailmanagement.admin.webservices;

import org.json.JSONObject;


public class JSONParse {

    public String Parse(JSONObject json) {
        try
        {
            return json.getString("Value");
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
    }
}
