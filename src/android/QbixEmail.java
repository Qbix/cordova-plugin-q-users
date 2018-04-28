package com.q.users.cordova.plugin;

import org.json.JSONObject;

public class QbixEmail {
    /**
     * TYPES
     *
     * TYPE_CUSTOM - 0 (actual type in {@link QbixEmail#customType})
     * TYPE_HOME - 1
     * TYPE_WORK - 2
     * TYPE_OTHER - 3
     */

    String address;
    int type;
    String customType = null;

    public JSONObject toJson() {
        try {
            JSONObject jsonEmail = new JSONObject();
            jsonEmail.put("address", address);
            jsonEmail.put("type", type);
            jsonEmail.put("customType", customType);
            return jsonEmail;
        } catch (Exception e) {
            return null;
        }
    }
}
