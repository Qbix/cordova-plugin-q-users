package com.q.users.cordova.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.q.users.cordova.plugin.QbixGroup;

import java.util.ArrayList;
import java.util.List;

public class QUsersCordova extends CordovaPlugin {

    //Actions
    private final String GET_ALL_LABELS_ACTION = "getAll";
    private final String GET_ONE_OR_MORE_LABELS_ACTION = "get";
    private final String REMOVE_CONTACT_FROM_LABEL_ACTION = "removeContact";
    private final String ADD_CONTACT_TO_LABEL_ACTION = "addContact";
    private final String REMOVE_LABEL_ACTION = "remove";
    private final String SAVE_NEW_LABEL_OR_EDIT = "save";
    private final String SET_LABEL_LIST_FOR_CONTACT = "setForContact";
    private final String GET_NATIVE_LABEL_FOR_CONTACT = "forContacts";
    private final String SMART = "smart";

    private final String READ = Manifest.permission.READ_CONTACTS;
    private final String WRITE = Manifest.permission.WRITE_CONTACTS;
    private final String ACCOUNTS = Manifest.permission.GET_ACCOUNTS;

    //Request code for the permissions picker (Pick is async and uses intents)
    private final int ALL_LABELS_REQ_CODE = 8;
    private final int LABELS_BY_SOURCE_ID_REQ_CODE = 9;
    private final int REMOVE_CONTACT_FROM_LABEL_REQ_CODE = 10;
    private final int ADD_CONTACT_TO_LABEL_REQ_CODE = 11;
    private final int REMOVE_LABEL_REQ_CODE = 12;
    private final int SAVE_NEW_LABEL_OR_EDIT_REQ_CODE = 13;
    private final int SET_LABEL_LIST_FOR_CONTACT_REQ_CODE = 14;
    private final int GET_NATIVE_LABEL_FOR_CONTACT_REQ_CODE = 15;
    private final int SMART_REQ_CODE = 16;

    //smart names
    /**
     * Get contacts which dont belong to any group.
     */
    protected static final String UNCATEGORIZED_SMART_NAME = "uncategorized";
    /**
     * Get contacts sorted by time added.
     */
    protected static final String BY_TIME_ADDED_SMART_NAME = "byTimeAdded";
    /**
     * Get contacts that have filled the "Company" or "Organization" field
     */
    protected static final String BY_COMPANY_SMART_NAME = "byCompany";
    /**
     * Get contacts that have "email" field
     */
    protected static final String HAS_EMAIL_SMART_NAME = "hasEmail";
    /**
     * Get contacts that have "phone" field
     */
    protected static final String HAS_PHONE_SMART_NAME = "hasPhone";
    /**
     * Get contacts that have photos
     */
    protected static final String HAS_PHOTO_SMART_NAME = "hasPhoto";

    //Error codes for returning with error plugin result
    protected static final String UNKNOWN_ERROR = "unknown error";
    protected static final String SUCCESS = "success";
    protected static final String NOT_SUPPORTED_ERROR = "not supported error";
    protected static final String PERMISSION_DENIED_ERROR = "permission denied error";

    private JSONArray executeArgs;
    private GroupAccessor groupAccessor;
    private CallbackContext callbackContext;   // The callback context from which we were invoked.

    /**
     * Constructor.
     */
    public QUsersCordova() {
    }

    private void getReadPermission(int requestCode) {
        PermissionHelper.requestPermission(this, requestCode, READ);
    }

    private void getWritePermission(int requestCode) {
        PermissionHelper.requestPermission(this, requestCode, WRITE);
    }

    private void getAccountPermission(int requestCode) {
        PermissionHelper.requestPermission(this, requestCode, ACCOUNTS);
    }

    /**
     * Requests 2 permissions at the same time.
     *
     * @param requestCode Request code
     * @param first       First permission name
     * @param second      Second permission name
     */
    private void getDoublePermission(int requestCode, String first, String second) {
        PermissionHelper.requestPermissions(this, requestCode, new String[]{first, second});
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArray of arguments for the plugin.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return True if the action was valid, false otherwise.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {

        QUsersCordova.this.callbackContext = callbackContext;
        this.executeArgs = args;
        /**
         * Check to see if we are on an Android 1.X device.  If we are return an error as we
         * do not support this as of Cordova 1.0.
         */
        if (android.os.Build.VERSION.RELEASE.startsWith("1.")) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, NOT_SUPPORTED_ERROR));
            return true;
        }

        /**
         * Only create the groupAccessor after we check the Android version or the program will crash
         * older phones.
         */
        if (this.groupAccessor == null) {
            this.groupAccessor = new GroupAccessor(this.cordova);
        }

        if (action.equals(GET_ALL_LABELS_ACTION)) {
            if (PermissionHelper.hasPermission(this, READ)) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        getLabels();
                    }
                });
            } else {
                getReadPermission(ALL_LABELS_REQ_CODE);
            }

            return true;
        }else if(action.equals(GET_ONE_OR_MORE_LABELS_ACTION)){
            if (PermissionHelper.hasPermission(this, READ) && PermissionHelper.hasPermission(this, ACCOUNTS)) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                            getLabels(executeArgs);
                    }
                });
            } else {
                getDoublePermission(LABELS_BY_SOURCE_ID_REQ_CODE, READ, ACCOUNTS);
            }
        } else if (action.equals(REMOVE_CONTACT_FROM_LABEL_ACTION)) {
            if (PermissionHelper.hasPermission(this, WRITE) && PermissionHelper.hasPermission(this, ACCOUNTS)) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        removeContactFromLabel(executeArgs);
                    }
                });
            } else {
                getDoublePermission(REMOVE_CONTACT_FROM_LABEL_REQ_CODE, WRITE, ACCOUNTS);
            }
            return true;
        } else if (action.equals(ADD_CONTACT_TO_LABEL_ACTION)) {
            if (PermissionHelper.hasPermission(this, WRITE) && PermissionHelper.hasPermission(this, ACCOUNTS)) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {                  
                            addContactToLabel(executeArgs);
                    }
                });
            } else {
                getDoublePermission(ADD_CONTACT_TO_LABEL_REQ_CODE, WRITE, ACCOUNTS);
            }
            return true;
        } else if (action.equals(REMOVE_LABEL_ACTION)) {
            if (PermissionHelper.hasPermission(this, WRITE) && PermissionHelper.hasPermission(this, ACCOUNTS)) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        removeLabelFromDatabase(executeArgs);
                    }
                });
            } else {
                getDoublePermission(REMOVE_LABEL_REQ_CODE, WRITE, ACCOUNTS);
            }
            return true;
        } else if (action.equals(SAVE_NEW_LABEL_OR_EDIT)) {
            if (PermissionHelper.hasPermission(this, WRITE) && PermissionHelper.hasPermission(this, ACCOUNTS)) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        saveOrEditLabel(executeArgs);
                    }
                });
            } else {
                getDoublePermission(SAVE_NEW_LABEL_OR_EDIT_REQ_CODE, WRITE, ACCOUNTS);
            }
        } else if (action.equals(SET_LABEL_LIST_FOR_CONTACT)) {
            if (PermissionHelper.hasPermission(this, WRITE) && PermissionHelper.hasPermission(this, ACCOUNTS)) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        setLabelListForContact(executeArgs);
                    }
                });
            } else {
                getDoublePermission(SET_LABEL_LIST_FOR_CONTACT_REQ_CODE, WRITE, ACCOUNTS);
            }
        } else if (action.equals(GET_NATIVE_LABEL_FOR_CONTACT)) {
            if (PermissionHelper.hasPermission(this, READ) && PermissionHelper.hasPermission(this, ACCOUNTS)) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        getLabelsForContact(executeArgs);
                    }
                });
            } else {
                getDoublePermission(GET_NATIVE_LABEL_FOR_CONTACT_REQ_CODE, READ, ACCOUNTS);
            }
        } else if (action.equals(SMART)) {
            if (PermissionHelper.hasPermission(this, READ) && PermissionHelper.hasPermission(this, ACCOUNTS)) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        smart(executeArgs);
                    }
                });
            } else {
                getDoublePermission(SMART_REQ_CODE, READ, ACCOUNTS);
            }
        }
        return false;
    }

    /**
     * Gets all labels asynchronously and set result to callback context's as success.
     */
    private void getLabels() {
        List<QbixGroup> labels = groupAccessor.getAllLabels();
        JSONArray jsonGroups = new JSONArray();
        for (QbixGroup group :
                labels) {
            jsonGroups.put(group.toJson());
        }
        if (labels != null) {
            callbackContext.success(jsonGroups);
        } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, UNKNOWN_ERROR));
        }
    }

    /**
     * Gets all labels that have given sourceIds and set result to callback context's as success.
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void getLabels(JSONArray args) {
        try {
            List<String> sourceIdList = new ArrayList<>();
            for (int i = 0; i < args.length(); i++) {
                JSONObject sourceIdJson = args.getJSONObject(i);
                String sourceId = sourceIdJson.getString("labelId");
                sourceIdList.add(sourceId);
            }
            String[] sourceIdArray = new String[sourceIdList.size()];
            for (int i = 0; i < sourceIdList.size(); i++) {
                sourceIdArray[i] = sourceIdList.get(i);
            }
            List<QbixGroup> labels = groupAccessor.getLabelsBySourceId(sourceIdArray);
            JSONArray jsonGroups = new JSONArray();
            for (QbixGroup group :
                    labels) {
                jsonGroups.put(group.toJson());
            }
            if (labels != null) {
                callbackContext.success(jsonGroups);
            } else {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, UNKNOWN_ERROR));
            }
        } catch (JSONException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
        }
    }

    /**
     * Removes contacts from given label
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void removeContactFromLabel(JSONArray args) {
        try {
            final JSONObject filter = args.getJSONObject(0);
            final String labelId = filter.getString("labelId");
            final JSONArray contactIds = filter.getJSONArray("contactIds");
            String[] idArray = new String[contactIds.length()];
            for (int i = 0; i < contactIds.length(); i++) {
                JSONObject row = contactIds.getJSONObject(i);
                idArray[i] = row.getString("contactId");
            }
            String removeMessage = groupAccessor.removeLabelFromContacts(labelId, idArray);
            if (removeMessage.equals(SUCCESS)) {
                callbackContext.success();
            } else {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, removeMessage));
            }
        } catch (JSONException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
        }
    }

    /**
     * Adds label to given contacts
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void addContactToLabel(JSONArray args) {
        try {
            final JSONObject filter = args.getJSONObject(0);
            final String labelId = filter.getString("labelId");
            final JSONArray contactIds = filter.getJSONArray("contactIds");
            String[] idArray = new String[contactIds.length()];
            for (int i = 0; i < contactIds.length(); i++) {
                JSONObject row = contactIds.getJSONObject(i);
                idArray[i] = row.getString("contactId");
            }
            String addMessage = groupAccessor.addLabelToContacts(labelId, idArray);
            if (addMessage.equals(SUCCESS)) {
                callbackContext.success();
            } else if (addMessage.equals(UNKNOWN_ERROR)) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, addMessage));
            } else {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, addMessage));
            }
        } catch (JSONException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
        }
    }

    /**
     * Removes all labels with given sourceId.
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void removeLabelFromDatabase(JSONArray args) {
        try {
            final JSONObject filter = args.getJSONObject(0);
            final String sourceId = filter.getString("labelId");
            String removeMessage = groupAccessor.removeLabelFromData(sourceId);
            if (removeMessage.equals(SUCCESS)) {
                callbackContext.success();
            } else {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, removeMessage));
            }
        } catch (JSONException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
        }

    }

    /**
     * Edits existing label's title if sourceId specified and adds new label if not (sourceId = -1).
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void saveOrEditLabel(JSONArray args) {
        try {
            String sourceId = args.getString(0);
            String title = args.getString(1);
            if (sourceId.equals("-1")) {
                //for not specified sourceId (need to add new label)
                String addMessage = groupAccessor.addLabelToDatabase(title);
                if (addMessage.equals(SUCCESS)) {
                    callbackContext.success();
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, addMessage));
                }
            } else {
                //for specified sourceId (need to edit existing one)
                String editMessage = groupAccessor.editLabelInDatabase(sourceId, title);
                if (editMessage.equals(SUCCESS)) {
                    callbackContext.success();
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, editMessage));
                }
            }
        } catch (JSONException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
        }
    }

    /**
     * Sets list of labels to given contact (if list is empty, removes all existing non system related labels)
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void setLabelListForContact(JSONArray args) {
        try {
            String contactId = args.getString(0);
            JSONArray labelIds = args.getJSONArray(1);
            List<String> labelIdList = new ArrayList<>();
            for (int i = 0; i < labelIds.length(); i++) {
                labelIdList.add(labelIds.getString(i));
            }
            String removeMessage = groupAccessor.setLabelListForContact(contactId, labelIdList);
            if (removeMessage.equals(SUCCESS)) {
                callbackContext.success();
            } else {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, removeMessage));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
        }
    }

    /**
     * Gets labels of given contact ids.
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void getLabelsForContact(JSONArray args) {
        try {
            JSONArray contactIds = args.getJSONArray(0);
            List<String> contactIdList = new ArrayList<>();
            for (int i = 0; i < contactIds.length(); i++) {
                contactIdList.add(contactIds.getString(i));
            }
            boolean doUnion = args.getBoolean(1);
            List<QbixGroup> labels = groupAccessor.getLabelsByContactIds(contactIdList, doUnion);
            JSONArray jsonGroups = new JSONArray();
            for (QbixGroup group :
                    labels) {
                jsonGroups.put(group.toJson());
            }
            if (labels != null) {
                callbackContext.success(jsonGroups);
            } else {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, UNKNOWN_ERROR));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
        }
    }

    /**
     * Gets and sorts contacts depending on "smart name" which defined in args param.
     * (See description of names
     * {@link QUsersCordova#UNCATEGORIZED_SMART_NAME},
     * {@link QUsersCordova#BY_TIME_ADDED_SMART_NAME},
     * {@link QUsersCordova#BY_COMPANY_SMART_NAME},
     * {@link QUsersCordova#HAS_EMAIL_SMART_NAME},
     * {@link QUsersCordova#HAS_PHONE_SMART_NAME},
     * {@link QUsersCordova#HAS_PHOTO_SMART_NAME})
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void smart(JSONArray args) {
        try {
            String name = args.getString(0);
            List<QbixContact> contacts = groupAccessor.getContactList(name);
            if (contacts != null) {
                JSONArray jsonContacts = new JSONArray();
                for (QbixContact contact :
                        contacts) {
                    jsonContacts.put(contact.toJson());
                }
                callbackContext.success(jsonContacts);
            } else {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, UNKNOWN_ERROR));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                QUsersCordova.this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }
        switch (requestCode) {
            case ALL_LABELS_REQ_CODE:
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        getLabels();
                    }
                });
                break;
            case REMOVE_CONTACT_FROM_LABEL_REQ_CODE:
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                            removeContactFromLabel(executeArgs);
                    }
                });
                break;
            case ADD_CONTACT_TO_LABEL_REQ_CODE:
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        addContactToLabel(executeArgs);
                    }
                });
                break;
            case REMOVE_LABEL_REQ_CODE:
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        removeLabelFromDatabase(executeArgs);
                    }
                });
                break;
            case LABELS_BY_SOURCE_ID_REQ_CODE:
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                            getLabels(executeArgs);
                    }
                });
                break;
            case SAVE_NEW_LABEL_OR_EDIT_REQ_CODE:
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        saveOrEditLabel(executeArgs);
                    }
                });
                break;
            case SET_LABEL_LIST_FOR_CONTACT_REQ_CODE:
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        setLabelListForContact(executeArgs);
                    }
                });
                break;
            case GET_NATIVE_LABEL_FOR_CONTACT_REQ_CODE:
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        getLabelsForContact(executeArgs);
                    }
                });
                break;
            case SMART_REQ_CODE:
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        smart(executeArgs);
                    }
                });
                break;
        }
    }

    /**
     * This plugin launches an external Activity when a contact is picked, so we
     * need to implement the save/restore API in case the Activity gets killed
     * by the OS while it's in the background. We don't actually save anything
     * because picking a contact doesn't take in any arguments.
     */
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        QUsersCordova.this.callbackContext = callbackContext;
        this.groupAccessor = new GroupAccessor(this.cordova);
    }
}
