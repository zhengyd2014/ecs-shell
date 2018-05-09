package com.emc.ecs.util;

import com.google.gson.Gson;
import org.apache.tapestry5.json.JSONObject;

/**
 * Created by zhengf1 on 12/22/16.
 */
public class JSONUtil {

    /**
     * if return ECS response is in JSON format, call this method to tranfer it to JSONObject
     *
     * @param response
     * @return
     */
    public static JSONObject responseToJSONObject(Object response) {
        String result = (new Gson()).toJson(response);
        JSONObject stat = new JSONObject(result);
        return stat;
    }
}
