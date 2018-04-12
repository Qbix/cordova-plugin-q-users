package com.q.users.cordova.plugin;

import org.json.JSONObject;

public class QbixGroup {
    String id;
    String sourceId;
    String notes;
    int summaryCount;
    boolean isVisible;
    boolean isDeleted;
    boolean shouldSync;
    boolean readOnly;

    public JSONObject toJson() {
        try {
            JSONObject jsonGroup = new JSONObject();
            jsonGroup.put("id", id);
            jsonGroup.put("sourceId", sourceId);
            jsonGroup.put("notes", notes);
            jsonGroup.put("summaryCount", summaryCount);
            jsonGroup.put("isVisible", isVisible);
            jsonGroup.put("isDeleted", isDeleted);
            jsonGroup.put("shouldSync", shouldSync);
            jsonGroup.put("readOnly", readOnly);
            return jsonGroup;
        } catch (Exception e) {
            return null;
        }
    }
}
