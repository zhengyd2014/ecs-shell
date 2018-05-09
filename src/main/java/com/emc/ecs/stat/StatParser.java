package com.emc.ecs.stat;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

/**
 * Created by zhengf1 on 11/29/16.
 */
public class StatParser {

    public static StatGroup parseGroup(JSONObject jsonObject) throws Exception {

        String type = (String)jsonObject.get("type");
        if (!type.equals("group")) {
            throw new Exception("not a group!");
        }

        StatGroup group = new StatGroup();
        group.setId((String)jsonObject.get("id"));
        group.setTimestamp(jsonObject.getLong("timestamp"));
        JSONArray groups = jsonObject.getJSONArray("groups");
        for (int i=0; i<groups.length(); i++) {
            group.addGroup(parseGroup(groups.getJSONObject(i)));
        }

        JSONArray primitives = jsonObject.getJSONArray("primitives");
        for (int i=0; i<primitives.length(); i++) {
            group.addPrimitive(parsePrimitive(primitives.getJSONObject(i)));
        }

        return group;
    }

    public static StatPrimitive parsePrimitive(JSONObject jsonObject) throws Exception {
        StatPrimitive primitive = new StatPrimitive();
        primitive.setId(jsonObject.getString("id"));
        primitive.setTimestamp(jsonObject.getString("timestamp"));
        if (jsonObject.getString("type").equals("counter")) {
            primitive.setCounter(jsonObject.getLong("counter"));
        } else {
            // throw new Exception("not support " + jsonObject.getString("type"));
        }

        return primitive;
    }


    /**
     * start searching from ancester, to find specific StatPrimitive by id
     *
     * @param ancester
     * @param id
     * @return
     */
    public static StatPrimitive findPrimitive(StatGroup ancester, String id) {
        if (id == null || id.trim().length() == 0) {
            return null;
        }

        if (ancester.getPrimitives() != null && ancester.getPrimitives().size() != 0) {
            for (StatPrimitive p : ancester.getPrimitives()) {
                if (p.getId().equals(id)) {
                    return p;
                }
            }
        }

        if (ancester.getGroups() != null && ancester.getGroups().size() != 0) {
            for (StatGroup group : ancester.getGroups()) {
                if (findPrimitive(group, id) != null) {
                    return findPrimitive(group, id);
                }
            }
        }

        return null;
    }
}
