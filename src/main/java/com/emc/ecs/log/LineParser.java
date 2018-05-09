package com.emc.ecs.log;

/**
 * Created by zhengf1 on 1/13/17.
 */
public interface LineParser {

    public LineParseResult parseLine(String line) throws Exception;

}
