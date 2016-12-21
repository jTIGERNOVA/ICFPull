package com.ibm.haac.hx.engine.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by antonioj on 6/14/2016.
 */
public class ICFClassification implements Comparable<ICFClassification> {

    public static final String KEY_CHILDREN = "children";

    private final List<ICFClassification> childClassifications;
    private final JsonObject jsonObject;
    private ICFClassification parentClassification;
    private String prefix;
    private String code;
    private int intCode;
    private int depthLevel;
    private String title;
    private String description;

    public ICFClassification() {
        childClassifications = new ArrayList<>(10);
        jsonObject = new JsonObject();
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getDepthLevel() {
        return depthLevel;
    }

    public void setDepthLevel(int depthLevel) {
        this.depthLevel = depthLevel;
    }

    public int getIntCode() {
        return intCode;
    }

    public void setIntCode(int intCode) {
        this.intCode = intCode;
    }

    public ICFClassification getParentClassification() {
        return parentClassification;
    }

    public void setParentClassification(ICFClassification parentClassification) {
        this.parentClassification = parentClassification;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addChildClassifications(ICFClassification child) {
        childClassifications.add(child);

        if (jsonObject.has(KEY_CHILDREN)) {
            JsonArray array = jsonObject.getAsJsonArray(KEY_CHILDREN);

            array.add(child.getJsonObject());
        } else {
            JsonArray array = new JsonArray();

            array.add(child.getJsonObject());

            jsonObject.add(KEY_CHILDREN, array);
        }
    }

    public boolean hasChildren() {
        return !childClassifications.isEmpty();
    }

    public JsonObject getJsonObject() {
        jsonObject.addProperty("code", code);
        jsonObject.addProperty("title", title);
        jsonObject.addProperty("description", description);

        return jsonObject;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        String parent = (parentClassification == null) ? "n/a" : parentClassification.code;

        builder.append(String.format("%s ICFClassification=[code=%s, parent=%s]", code,
                code, parent)).append("\n");

        Collections.sort(childClassifications);

        for (ICFClassification classification : childClassifications) {
            for (int i = 0; i <= depthLevel; i++)
                builder.append("\t");

            builder.append(">>").append(classification.toString());
        }

        return builder.toString();
    }


    @Override
    public int compareTo(ICFClassification o) {
        if (o == null)
            return 1;

        return Integer.compare(intCode, o.intCode);
    }
}
