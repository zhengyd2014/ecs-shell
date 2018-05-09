package com.emc.ecs.stat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengf1 on 12/2/16.
 */
public class StatGroup {

    private String id;
    private long timestamp;
    private List<StatGroup> groups;
    private List<StatPrimitive> primitives;

    public void setId(String id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void addGroup(StatGroup group) {
        if (groups == null) {
            groups = new ArrayList<StatGroup>();
        }

        groups.add(group);
    }

    public void addPrimitive(StatPrimitive primitive) {
        if (primitives == null) {
            this.primitives = new ArrayList<StatPrimitive>();
        }

        primitives.add(primitive);
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<StatGroup> getGroups() {
        return this.groups;
    }

    public List<StatPrimitive> getPrimitives() {
        return this.primitives;
    }
}
