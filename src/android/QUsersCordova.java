package com.q.users.cordova.plugin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private final String CHECK_CONTACTS_ACCOUNT = "checkContactsAccount";
    private final String CHECK_LABELS_ACCOUNT = "checkLabelsAccount";

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
    private final int CHECK_CONTACTS_ACCOUNT_REQ_CODE = 17;
    private final int CHECK_LABELS_ACCOUNT_REQ_CODE = 18;

    //smart names
    /**
     * Get contacts which dont belong to any group.
     */
    protected static final String UNCATEGORIZED_SMART_NAME = "uncategorized";
    /**
     * Get contacts sorted by last time updated.
     */
    protected static final String BY_LAST_TIME_UPDATED_SMART_NAME = "byTimeAdded";
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
    protected static final String UNKNOWN_ERROR = "Unknown error.";
    protected static final String INVALID_DATA_ERROR = "Invalid data.";
    protected static final String SUCCESS = "Success";
    protected static final String NO_ACCOUNT_ERROR = "No accounts bound to device. There is no labels for local contacts.";
    protected static final String MISSING_CONTACT_ERROR = "There is no contactId(s).";
    protected static final String MISSING_LABEL_ERROR = "There is no labelId(s).";
    protected static final String READ_ONLY_LABEL_ERROR = "LabelId is read only and cannot be removed.";
    protected static final String NOT_SUPPORTED_ERROR = "Not supporting version.";
    protected static final String PERMISSION_DENIED_ERROR = "Permission denied.";

    private JSONArray executeArgs;
    private GroupAccessor groupAccessor;
    private CallbackContext callbackContext;   // The callback context from which we were invoked.

    /**
     * Constructor.
     */
    public QUsersCordova() {
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
            if (PermissionHelper.hasPermission(this, READ) && PermissionHelper.hasPermission(this, ACCOUNTS)) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        getLabels();
                    }
                });
            } else {
                getDoublePermission(ALL_LABELS_REQ_CODE, READ, ACCOUNTS);
            }

            return true;
        } else if (action.equals(GET_ONE_OR_MORE_LABELS_ACTION)) {
            if (PermissionHelper.hasPermission(this, READ) && PermissionHelper.hasPermission(this, ACCOUNTS)) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        getLabels(executeArgs);
                    }
                });
            } else {
                getDoublePermission(LABELS_BY_SOURCE_ID_REQ_CODE, READ, ACCOUNTS);
            }

            return true;
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

            return true;
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

            return true;
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

            return true;
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

            return true;
        } else if (action.equals(CHECK_CONTACTS_ACCOUNT)) {
            if (PermissionHelper.hasPermission(this, READ) && PermissionHelper.hasPermission(this, ACCOUNTS)) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        checkContactsAccount(executeArgs);
                    }
                });
            } else {
                getDoublePermission(CHECK_CONTACTS_ACCOUNT_REQ_CODE, READ, ACCOUNTS);
            }

            return true;
        } else if (action.equals(CHECK_LABELS_ACCOUNT)) {
            if (PermissionHelper.hasPermission(this, READ) && PermissionHelper.hasPermission(this, ACCOUNTS)) {
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        checkLabelsAccount(executeArgs);
                    }
                });
            } else {
                getDoublePermission(CHECK_LABELS_ACCOUNT_REQ_CODE, READ, ACCOUNTS);
            }

            return true;
        }
        return false;
    }

    /**
     * Gets all labels asynchronously and set result to callback context's as success.
     */
    private void getLabels() {
        if (ValidationUtil.doesDeviceHasAccounts(this.cordova.getActivity())) {
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
        } else {
            this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, NO_ACCOUNT_ERROR));
        }

    }

    /**
     * Gets all labels that have given sourceIds and set result to callback context's as success.
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void getLabels(JSONArray args) {
        if (ValidationUtil.doesDeviceHasAccounts(this.cordova.getActivity())) {
            try {
                JSONArray labelIdArray = args.getJSONArray(0);
                boolean idsAreValid = true;
                for (int i = 0; i < labelIdArray.length(); i++) {
                    if (!ValidationUtil.nullOrEmptyChecker(labelIdArray.getString(i))) {
                        idsAreValid = false;
                    }
                }
                if (idsAreValid && !ValidationUtil.isArrayEmpty(labelIdArray)) {
                    String[] sourceIdArray = new String[labelIdArray.length()];
                    for (int i = 0; i < labelIdArray.length(); i++) {
                        sourceIdArray[i] = labelIdArray.getString(i);
                    }
                    String[] filteredArray = ValidationUtil.cleanFromDuplicates(sourceIdArray);
                    List<QbixGroup> labels = groupAccessor.getLabelsBySourceId(filteredArray);
                    JSONArray jsonGroups = new JSONArray();
                    for (QbixGroup group :
                            labels) {
                        jsonGroups.put(group.toJson());
                    }
                    callbackContext.success(jsonGroups);
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, INVALID_DATA_ERROR));
                }
            } catch (JSONException e) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
            }
        } else {
            this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, NO_ACCOUNT_ERROR));
        }
    }

    /**
     * Removes contacts from given label
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void removeContactFromLabel(JSONArray args) {
        if (ValidationUtil.doesDeviceHasAccounts(this.cordova.getActivity())) {
            try {
                String labelId = args.getString(0);
                boolean labelIdIsValid = ValidationUtil.nullOrEmptyChecker(labelId);
                JSONArray contactIds = args.getJSONArray(1);
                boolean idsAreValid = true;
                for (int i = 0; i < contactIds.length(); i++) {
                    if (!ValidationUtil.canCastToInt(contactIds.getString(i))) {
                        idsAreValid = false;
                    }
                }
                if (labelIdIsValid && idsAreValid && !ValidationUtil.isArrayEmpty(contactIds)) {
                    String[] idArray = new String[contactIds.length()];
                    for (int i = 0; i < contactIds.length(); i++) {
                        idArray[i] = contactIds.getString(i);
                    }
                    String[] filteredArray = ValidationUtil.cleanFromDuplicates(idArray);
                    String removeMessage = groupAccessor.removeLabelFromContacts(labelId, filteredArray);
                    if (removeMessage.equals(SUCCESS)) {
                        callbackContext.success();
                    } else {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, removeMessage));
                    }
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, INVALID_DATA_ERROR));
                }
            } catch (JSONException e) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
            }
        } else {
            this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, NO_ACCOUNT_ERROR));
        }
    }

    /**
     * Adds label to given contacts
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void addContactToLabel(JSONArray args) {
        if (ValidationUtil.doesDeviceHasAccounts(this.cordova.getActivity())) {
            try {
                String labelId = args.getString(0);
                boolean labelIdIsValid = ValidationUtil.nullOrEmptyChecker(labelId);
                JSONArray contactIds = args.getJSONArray(1);
                boolean idsAreValid = true;
                for (int i = 0; i < contactIds.length(); i++) {
                    if (!ValidationUtil.canCastToInt(contactIds.getString(i))) {
                        idsAreValid = false;
                    }
                }
                if (labelIdIsValid && idsAreValid && !ValidationUtil.isArrayEmpty(contactIds)) {
                    String[] idArray = new String[contactIds.length()];
                    for (int i = 0; i < contactIds.length(); i++) {
                        idArray[i] = contactIds.getString(i);
                    }
                    String[] filteredArray = ValidationUtil.cleanFromDuplicates(idArray);
                    String addMessage = groupAccessor.addLabelToContacts(labelId, filteredArray);
                    if (addMessage.equals(SUCCESS)) {
                        callbackContext.success();
                    } else {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, addMessage));
                    }
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, INVALID_DATA_ERROR));
                }
            } catch (JSONException e) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
            }
        } else {
            this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, NO_ACCOUNT_ERROR));
        }
    }

    /**
     * Removes all labels with given sourceId.
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void removeLabelFromDatabase(JSONArray args) {
        if (ValidationUtil.doesDeviceHasAccounts(this.cordova.getActivity())) {
            try {
                String sourceId = args.getString(0);
                boolean sourceIdIsValid = ValidationUtil.nullOrEmptyChecker(sourceId);
                if (sourceIdIsValid) {
                    String removeMessage = groupAccessor.removeLabelFromData(sourceId);
                    if (removeMessage.equals(SUCCESS)) {
                        callbackContext.success();
                    } else {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, removeMessage));
                    }
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, INVALID_DATA_ERROR));
                }
            } catch (JSONException e) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
            }
        } else {
            this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, NO_ACCOUNT_ERROR));
        }
    }

    /**
     * Edits existing label's title if sourceId specified and adds new label if not (sourceId = -1).
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void saveOrEditLabel(JSONArray args) {
        if (ValidationUtil.doesDeviceHasAccounts(this.cordova.getActivity())) {
            try {
                String sourceId = args.getString(0);
                boolean sourceIdIsValid = ValidationUtil.nullOrEmptyChecker(sourceId);
                String title = args.getString(1);
                boolean titleIsValid = ValidationUtil.nullOrEmptyChecker(title);
                if (sourceIdIsValid && titleIsValid) {
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
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, INVALID_DATA_ERROR));
                }
            } catch (JSONException e) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
            }
        } else {
            this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, NO_ACCOUNT_ERROR));
        }
    }

    /**
     * Sets list of labels to given contact (if list is empty, removes all existing non system related labels)
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void setLabelListForContact(JSONArray args) {
        if (ValidationUtil.doesDeviceHasAccounts(this.cordova.getActivity())) {
            try {
                String contactId = args.getString(0);
                boolean contactIdIsValid = ValidationUtil.canCastToInt(contactId);
                JSONArray labelIds = args.getJSONArray(1);
                boolean idsAreValid = true;
                if (labelIds.length() != 0) {     //empty array means all non-system-related labels removal.
                    for (int i = 0; i < labelIds.length(); i++) {
                        if (!ValidationUtil.nullOrEmptyChecker(labelIds.getString(i))) {
                            idsAreValid = false;
                        }
                    }
                }
                if (contactIdIsValid && idsAreValid) {
                    String[] idArray = new String[labelIds.length()];
                    for (int i = 0; i < labelIds.length(); i++) {
                        idArray[i] = labelIds.getString(i);
                    }
                    String[] filteredArray = ValidationUtil.cleanFromDuplicates(idArray);
                    String removeMessage = groupAccessor.setLabelListForContact(contactId, filteredArray);
                    if (removeMessage.equals(SUCCESS)) {
                        callbackContext.success();
                    } else {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, removeMessage));
                    }
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, INVALID_DATA_ERROR));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
            }
        } else {
            this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, NO_ACCOUNT_ERROR));
        }
    }

    /**
     * Gets labels of given contact ids.
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void getLabelsForContact(JSONArray args) {
        if (ValidationUtil.doesDeviceHasAccounts(this.cordova.getActivity())) {
            try {
                JSONArray contactIds = args.getJSONArray(0);
                boolean contactIdIsValid = true;
                for (int i = 0; i < contactIds.length(); i++) {
                    if (!ValidationUtil.canCastToInt(contactIds.getString(i))) {
                        contactIdIsValid = false;
                    }
                }
                if (contactIdIsValid && !ValidationUtil.isArrayEmpty(contactIds)) {
                    List<String> contactIdList = new ArrayList<>();
                    for (int i = 0; i < contactIds.length(); i++) {
                        contactIdList.add(contactIds.getString(i));
                    }
                    boolean doUnion = args.getBoolean(1);
                    List<String> filteredList = ValidationUtil.cleanFromDuplicates(contactIdList);
                    List<QbixGroup> labels = groupAccessor.getLabelsByContactIds(filteredList, doUnion);
                    JSONArray jsonGroups = new JSONArray();
                    for (QbixGroup group :
                            labels) {
                        jsonGroups.put(group.toJson());
                    }
                    callbackContext.success(jsonGroups);
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, INVALID_DATA_ERROR));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
            }
        } else {
            this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, NO_ACCOUNT_ERROR));
        }
    }

    /**
     * Gets and sorts contactIds depending on "smart name" which defined in args param.
     * (See description of names
     * {@link QUsersCordova#UNCATEGORIZED_SMART_NAME},
     * {@link QUsersCordova#BY_LAST_TIME_UPDATED_SMART_NAME},
     * {@link QUsersCordova#BY_COMPANY_SMART_NAME},
     * {@link QUsersCordova#HAS_EMAIL_SMART_NAME},
     * {@link QUsersCordova#HAS_PHONE_SMART_NAME},
     * {@link QUsersCordova#HAS_PHOTO_SMART_NAME})
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void smart(JSONArray args) {
        if (ValidationUtil.doesDeviceHasAccounts(this.cordova.getActivity())) {
            try {
                String name = args.getString(0);
                if (name != null
                        && (name.equals(UNCATEGORIZED_SMART_NAME)
                        || name.equals(BY_COMPANY_SMART_NAME)
                        || name.equals(BY_LAST_TIME_UPDATED_SMART_NAME)
                        || name.equals(HAS_EMAIL_SMART_NAME)
                        || name.equals(HAS_PHONE_SMART_NAME)
                        || name.equals(HAS_PHOTO_SMART_NAME))) {
                    String[] contacts = groupAccessor.getContactList(name);
                    if (contacts != null) {
                        JSONObject jsonSmart = new JSONObject();
                        jsonSmart.put("title", name);
                        JSONArray jsonContacts = new JSONArray();
                        for (int i = 0; i < contacts.length; i++) {
                            jsonContacts.put(contacts[i]);
                        }
                        jsonSmart.put("contactIds", jsonContacts);
                        callbackContext.success(jsonSmart);
                    } else {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, UNKNOWN_ERROR));
                    }
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, INVALID_DATA_ERROR));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
            }
        } else {
            this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, NO_ACCOUNT_ERROR));
        }
    }

    /**
     * Binds given contacts' ids to their account's name and type.
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void checkContactsAccount(JSONArray args) {
        if (ValidationUtil.doesDeviceHasAccounts(this.cordova.getActivity())) {
            try {
                JSONArray contactIds = args.getJSONArray(0);
                boolean contactIdIsValid = true;
                for (int i = 0; i < contactIds.length(); i++) {
                    if (!ValidationUtil.canCastToInt(contactIds.getString(i))) {
                        contactIdIsValid = false;
                    }
                }
                if (contactIdIsValid && !ValidationUtil.isArrayEmpty(contactIds)) {
                    String[] idsArray = new String[contactIds.length()];
                    for (int i = 0; i < contactIds.length(); i++) {
                        idsArray[i] = contactIds.getString(i);
                    }
                    String[] filteredArray = ValidationUtil.cleanFromDuplicates(idsArray);
                    List<AccountContactIds> accountContactIdsPairs = groupAccessor.getAccountContactIdsPairs(filteredArray);
                    JSONArray jsonAccNameContacts = new JSONArray();
                    for (int i = 0; i < accountContactIdsPairs.size(); i++) {
                        jsonAccNameContacts.put(accountContactIdsPairs.get(i).toJson());
                    }
                    callbackContext.success(jsonAccNameContacts);
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, INVALID_DATA_ERROR));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
            }
        } else {
            this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, NO_ACCOUNT_ERROR));
        }
    }

    /**
     * Binds given labels' ids to their account's name and type.
     *
     * @param args Arguments from {@link #execute(String, JSONArray, CallbackContext)} method
     */
    private void checkLabelsAccount(JSONArray args) {
        if (ValidationUtil.doesDeviceHasAccounts(this.cordova.getActivity())) {
            try {
                JSONArray sourceIds = args.getJSONArray(0);
                boolean contactIdIsValid = true;
                for (int i = 0; i < sourceIds.length(); i++) {
                    if (!ValidationUtil.nullOrEmptyChecker(sourceIds.getString(i))) {
                        contactIdIsValid = false;
                    }
                }
                if (contactIdIsValid && !ValidationUtil.isArrayEmpty(sourceIds)) {
                    String[] idsArray = new String[sourceIds.length()];
                    for (int i = 0; i < sourceIds.length(); i++) {
                        idsArray[i] = sourceIds.getString(i);
                    }
                    String[] filteredArray = ValidationUtil.cleanFromDuplicates(idsArray);
                    List<AccountLabelIds> accountLabelIdsPairs = groupAccessor.getAccountSourceIdsPairs(filteredArray);
                    JSONArray jsonAccNameLabels = new JSONArray();
                    for (int i = 0; i < accountLabelIdsPairs.size(); i++) {
                        jsonAccNameLabels.put(accountLabelIdsPairs.get(i).toJson());
                    }
                    callbackContext.success(jsonAccNameLabels);
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, INVALID_DATA_ERROR));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage()));
            }
        } else {
            this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, NO_ACCOUNT_ERROR));
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
            case CHECK_CONTACTS_ACCOUNT_REQ_CODE:
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        checkContactsAccount(executeArgs);
                    }
                });
                break;
            case CHECK_LABELS_ACCOUNT_REQ_CODE:
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        checkLabelsAccount(executeArgs);
                    }
                });
                break;
        }
    }

    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        QUsersCordova.this.callbackContext = callbackContext;
        this.groupAccessor = new GroupAccessor(this.cordova);
    }
}
