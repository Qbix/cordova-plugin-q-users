package com.q.users.cordova.plugin;

import org.json.JSONObject;

public class QbixName {

    String displayName;
    String givenName;
    String familyName;
    String prefix;  //Common prefixes in English names are "Mr", "Ms", "Dr" etc.
    String middleName;
    String suffix;  //Common suffixes in English names are "Sr", "Jr", "III" etc.
    String phoneticGivenName;   //Used for phonetic spelling of the name, e.g. Pinyin, Katakana, Hiragana
    String phoneticMiddleName;
    String phoneticFamilyName;

    public JSONObject toJson() {
        try {
            JSONObject jsonName = new JSONObject();
            jsonName.put("displayName", displayName);
            jsonName.put("givenName", givenName);
            jsonName.put("familyName", familyName);
            jsonName.put("prefix", prefix);
            jsonName.put("middleName", middleName);
            jsonName.put("suffix", suffix);
            jsonName.put("phoneticGivenName", phoneticGivenName);
            jsonName.put("phoneticMiddleName", phoneticMiddleName);
            jsonName.put("phoneticFamilyName", phoneticFamilyName);
            return jsonName;
        } catch (Exception e) {
            return null;
        }
    }
}
