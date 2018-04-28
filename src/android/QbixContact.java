package com.q.users.cordova.plugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QbixContact {


    String displayName;
    QbixName name;
    List<QbixOrganization> organizations;
    List<QbixAddress> addresses;
    List<QbixPhone> phones;
    List<QbixEmail> emails;
    List<QbixIm> ims;
    List<QbixWebsite> websites;
    List<QbixPhoto> photos;
    String note;
    String nickname;
    String birthday;

    public QbixContact() {
        displayName = "";
        name = new QbixName();
        organizations = new ArrayList<>();
        addresses = new ArrayList<>();
        phones = new ArrayList<>();
        emails = new ArrayList<>();
        ims = new ArrayList<>();
        websites = new ArrayList<>();
        photos =new ArrayList<>();
        note = "";
        nickname = "";
        birthday = "";
    }

    public JSONObject toJson(){
        JSONObject jsonContact = new JSONObject();
        try {
            jsonContact.put("displayName", displayName);
            jsonContact.put("displayName", name.toJson());

            JSONArray jsonOrganizations = new JSONArray();
            for (int i = 0; i < organizations.size(); i++) {
                jsonOrganizations.put(organizations.get(i).toJson());
            }
            jsonContact.put("organizations", jsonOrganizations);

            JSONArray jsonAddresses = new JSONArray();
            for (int i = 0; i < addresses.size(); i++) {
                jsonAddresses.put(addresses.get(i).toJson());
            }
            jsonContact.put("addresses", jsonAddresses);

            JSONArray jsonPhones = new JSONArray();
            for (int i = 0; i < phones.size(); i++) {
                jsonPhones.put(phones.get(i).toJson());
            }
            jsonContact.put("phones", jsonPhones);

            JSONArray jsonEmails = new JSONArray();
            for (int i = 0; i < emails.size(); i++) {
                jsonEmails.put(emails.get(i).toJson());
            }
            jsonContact.put("emails", emails);

            JSONArray jsonIms = new JSONArray();
            for (int i = 0; i < ims.size(); i++) {
                jsonIms.put(ims.get(i).toJson());
            }
            jsonContact.put("ims", jsonIms);

            JSONArray jsonWebsites = new JSONArray();
            for (int i = 0; i < websites.size(); i++) {
                jsonWebsites.put(websites.get(i).toJson());
            }
            jsonContact.put("websites", jsonWebsites);

            JSONArray jsonPhotos = new JSONArray();
            for (int i = 0; i < phones.size(); i++) {
                jsonPhotos.put(photos.get(i).toJson());
            }
            jsonContact.put("photos", jsonPhotos);

            jsonContact.put("note", note);
            jsonContact.put("nickname", nickname);
            jsonContact.put("birthday", birthday);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonContact;
    }
}
