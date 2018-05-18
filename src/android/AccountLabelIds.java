package com.q.users.cordova.plugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AccountLabelIds {

    public String accName;
    public String accType;
    public List<String> sourceIds = new ArrayList<>();

    public AccountLabelIds(String accName, String accType) {
        this.accName = accName;
        this.accType = accType;
    }

    public JSONObject toJson() {
        JSONObject accNameIdsJson = new JSONObject();
        try {
            accNameIdsJson.put("accountName", accName);
            accNameIdsJson.put("accountType", accType);
            JSONArray labelIdsArray = new JSONArray();
            for (int i = 0; i < sourceIds.size(); i++) {
                labelIdsArray.put(sourceIds.get(i));
            }
            accNameIdsJson.put("labelIds", labelIdsArray);
            return accNameIdsJson;
        } catch (JSONException e) {
            return null;
        }
    }
}
