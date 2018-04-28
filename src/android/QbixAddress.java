package com.q.users.cordova.plugin;

import org.json.JSONObject;

public class QbixAddress {

    /**
     * TYPES
     *
     * TYPE_CUSTOM - 0 (actual type in {@link QbixAddress#customType})
     * TYPE_HOME - 1
     * TYPE_WORK - 2
     * TYPE_OTHER - 3
     */

    String formattedAddress;
    int type;
    String customType;
    String street;
    String pobox;   //Post Office Box number
    String neighborhood;
    String city;
    String region;
    String postcode;
    String country;

    public JSONObject toJson() {
        try {
            JSONObject jsonAddress = new JSONObject();
            jsonAddress.put("formattedAddress", formattedAddress);
            jsonAddress.put("type", type);
            jsonAddress.put("customType", customType);
            jsonAddress.put("street", street);
            jsonAddress.put("pobox", pobox);
            jsonAddress.put("neighborhood", neighborhood);
            jsonAddress.put("city", city);
            jsonAddress.put("region", region);
            jsonAddress.put("postcode", postcode);
            jsonAddress.put("country", country);
            return jsonAddress;
        } catch (Exception e) {
            return null;
        }
    }
}
