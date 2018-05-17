package com.q.users.cordova.plugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AccountContactIds {

    public String accName;
    public String accType;
    public List<String> contactIds = new ArrayList<>();

    public AccountContactIds(String accName, String accType) {
        this.accName = accName;
        this.accType = accType;
    }

    public JSONObject toJson() {
        JSONObject accNameIdsJson = new JSONObject();
        try {
            accNameIdsJson.put("accountName", accName);
            accNameIdsJson.put("accountType", accType);
            JSONArray contactIdArray = new JSONArray();
            for (int i = 0; i < contactIds.size(); i++) {
                contactIdArray.put(contactIds.get(i));
            }
            accNameIdsJson.put("contactIds", contactIdArray);
            return accNameIdsJson;
        } catch (JSONException e) {
            return null;
        }
    }
}
