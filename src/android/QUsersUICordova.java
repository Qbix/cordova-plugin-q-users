package com.q.users.cordova.plugin;

import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class QUsersUICordova extends CordovaPlugin {
    private final int CREATE_NEW_CONTACT_REQ_CODE = 1010;
    private static CallbackContext createContactCallback = null;

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
        } else if(action.equalsIgnoreCase("create")) {
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            intent.putExtra("finishActivityOnSaveCompleted", true);
            createContactCallback = callbackContext;
            cordova.setActivityResultCallback(this);
            this.cordova.getActivity().startActivityForResult(intent, CREATE_NEW_CONTACT_REQ_CODE);

            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == CREATE_NEW_CONTACT_REQ_CODE) {
            if(createContactCallback == null)
                return;
            if(resultCode == RESULT_OK && intent.getData() != null) {
                List<String> pathSegments = intent.getData().getPathSegments();
                if(pathSegments.size() > 0) {
                    String contactId = pathSegments.get(pathSegments.size() - 1);
                    createContactCallback.success(contactId);
                } else {
                    createContactCallback.error("Smth wrong");
                }
            } else {
                createContactCallback.error("Smth wrong");
            }
            createContactCallback = null;
        }
    }
}
