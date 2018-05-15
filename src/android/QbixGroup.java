package com.q.users.cordova.plugin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class QbixGroup {

    public String sourceId;
    public String title;
    public String notes;
    public int summaryCount;
    public boolean isVisible;
    public boolean isDeleted;
    public boolean shouldSync;
    public boolean readOnly;
    public List<Integer> contactIds;

    public JSONObject toJson() {
        try {
            JSONObject jsonGroup = new JSONObject();
            jsonGroup.put("sourceId", sourceId!=null ? sourceId: "");
            jsonGroup.put("title", title!=null ? title: "");
            jsonGroup.put("notes", notes!=null ? notes: "");
            jsonGroup.put("summaryCount", summaryCount);
            jsonGroup.put("isVisible", isVisible);
            jsonGroup.put("isDeleted", isDeleted);
            jsonGroup.put("shouldSync", shouldSync);
            jsonGroup.put("readOnly", readOnly);
            JSONArray jsonContactIds = new JSONArray();
            for (int i = 0; i <contactIds.size(); i++) {
                jsonContactIds.put(contactIds.get(i));
            }
            jsonGroup.put("contactIds",jsonContactIds);
            return jsonGroup;
        } catch (Exception e) {
            return null;
        }
    }
}
