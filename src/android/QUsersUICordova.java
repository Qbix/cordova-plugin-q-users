package com.q.users.cordova.plugin;

import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;

public class QUsersUICordova extends CordovaPlugin {

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if(action.equalsIgnoreCase("show")) {
            try {
                int contactId = args.getInt(0);
                Intent contactIntent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.withAppendedPath(
                        ContactsContract.Contacts.CONTENT_URI,
                        String.valueOf(contactId));
                contactIntent.setData(uri);
                this.cordova.getActivity().startActivity(contactIntent);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
            } catch (Exception e) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
            }
            return true;
        }
        return false;
    }


}
