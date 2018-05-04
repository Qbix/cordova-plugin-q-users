package com.q.users.cordova.plugin;

import org.json.JSONObject;

public class QbixOrganization {

    /**
     * TYPES
     *
     * TYPE_CUSTOM - 0 (actual type in {@link QbixOrganization#customType})
     * TYPE_WORK - 1
     * TYPE_OTHER - 2
     *
     * PHONETIC_NAME_STYLE
     * JAPANESE - 4
     * KOREAN - 5
     * PINYIN - 3
     * UNDEFINED - 0
     */

    String company;
    int type;
    String customType;
    String title;
    String department;
    String jobDescription;
    String symbol;
    String phoneticName;
    String officeLocation;
    String phoneticNameStyle;

    public JSONObject toJson() {
        try {
            JSONObject jsonOrganization = new JSONObject();
            jsonOrganization.put("company", company);
            jsonOrganization.put("type", type);
            jsonOrganization.put("customType", customType);
            jsonOrganization.put("title", title);
            jsonOrganization.put("department", department);
            jsonOrganization.put("jobDescription", jobDescription);
            jsonOrganization.put("symbol", symbol);
            jsonOrganization.put("phoneticName", phoneticName);
            jsonOrganization.put("officeLocation", officeLocation);
            jsonOrganization.put("phoneticNameStyle", phoneticNameStyle);
            return jsonOrganization;
        } catch (Exception e) {
            return null;
        }
    }
}
