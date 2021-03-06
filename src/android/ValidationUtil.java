package com.q.users.cordova.plugin;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ValidationUtil {
    public static final int REQUEST_CODE_PICK_ACCOUNT = 21;

    /**
     * Checks if given String is not null or empty.
     *
     * @param s String which wanted to be checked.
     * @return true if valid, false if not.
     */
    protected static boolean nullOrEmptyChecker(String s) {
        return s != null && !s.equals("");
    }

    /**
     * Checks if given String be casted to int (numeric value).
     *
     * @param s String which wanted to be checked.
     * @return true if valid, false if not.
     */
    protected static boolean canCastToInt(String s) {
        try {
            Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Checks if given JSONArray is empty.
     *
     * @param array JSONArray wanted to be checked
     * @return true if empty, false if not
     */
    protected static boolean isArrayEmpty(JSONArray array) {
        return array == null || array.length() == 0;
    }

    /**
     * Builds message for missing object error message.
     *
     * @param missingObject Type of missing objects (contact, label, account)
     * @param missingIds    List of missing objects ids
     * @return String for sending as error message.
     */
    protected static String getMissingErrorMessage(String missingObject, List<String> missingIds) {
        String message = missingObject + "(s) with following id(s) is(are) missing: ";
        for (int i = 0; i < missingIds.size(); i++) {
            message += missingIds.get(i);
            if (i == (missingIds.size() - 1)) {
                message += ".";
            } else {
                message += ", ";
            }
        }
        return message;
    }

    /**
     * Checks if all contactIds are existing in database.
     *
     * @param context    Context instance for db interactions
     * @param contactIds Array of contactIds which wanted to be checked.
     * @return List of missing contacts' ids.
     */
    protected static List<String> getMissingContactIds(Context context, String[] contactIds) {
        List<String> existingContacts = new ArrayList<>();
        List<String> missingContacts = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                new String[]{
                        ContactsContract.Contacts._ID
                },
                ContactsContract.Contacts._ID + GroupHelper.getSuffix(contactIds.length),
                contactIds,
                null);
        while (cursor.moveToNext()) {
            if (!existingContacts.contains(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)))) {
                existingContacts.add(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)));
            }
        }
        for (int i = 0; i < contactIds.length; i++) {
            if (!existingContacts.contains(contactIds[i]) && !missingContacts.contains(contactIds[i])) {
                missingContacts.add(contactIds[i]);
            }
        }
        cursor.close();
        return missingContacts;
    }

    /**
     * Checks if all contactId is existing in database.
     *
     * @param context   Context instance for db interactions
     * @param contactId ContactId which wanted to be checked.
     * @return true if exists, false if not
     */
    protected static boolean isContactExisting(Context context, String contactId) {
        boolean isExisting = false;
        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                new String[]{
                        ContactsContract.Contacts._ID
                },
                ContactsContract.Contacts._ID + "='" + contactId + "'",
                null,
                null);
        while (cursor.moveToNext()) {
            isExisting = true;
        }
        cursor.close();
        return isExisting;
    }

    /**
     * Checks if all sourceIds are existing in database.
     *
     * @param context   Context instance for db interactions
     * @param sourceIds Array of sourceIds which wanted to be checked.
     * @return List of missing labels' ids.
     */
    protected static List<String> getMissingSourceIds(Context context, String[] sourceIds) {
        List<String> existingLabels = new ArrayList<>();
        List<String> missingLabels = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.Groups.CONTENT_URI,
                new String[]{
                        ContactsContract.Groups.SOURCE_ID
                },
                ContactsContract.Groups.SOURCE_ID + GroupHelper.getSuffix(sourceIds.length),
                sourceIds,
                null);
        while (cursor.moveToNext()) {
            if (!existingLabels.contains(cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.SOURCE_ID)))) {
                existingLabels.add(cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.SOURCE_ID)));
            }
        }
        for (int i = 0; i < sourceIds.length; i++) {
            if (!existingLabels.contains(sourceIds[i]) && !missingLabels.contains(sourceIds[i])) {
                missingLabels.add(sourceIds[i]);
            }
        }
        cursor.close();
        return missingLabels;
    }

    /**
     * Checks if all sourceId is existing in database.
     *
     * @param context  Context instance for db interactions
     * @param sourceId SourceId which wanted to be checked.
     * @return true if exists, false if not
     */
    protected static boolean isSourceIdExisting(Context context, String sourceId) {
        boolean isExisting = false;
        Cursor cursor = context.getContentResolver().query(ContactsContract.Groups.CONTENT_URI,
                new String[]{
                        ContactsContract.Groups.SOURCE_ID
                },
                ContactsContract.Groups.SOURCE_ID + "='" + sourceId + "'",
                null,
                null);
        while (cursor.moveToNext()) {
            isExisting = true;
        }
        cursor.close();
        return isExisting;
    }

    /**
     * Checks if there is accounts bound to device.
     * If not, all actions are pointless, because there is no label field in local contact.
     *
     * @param context Context for getting account manager
     * @return true if there is at least 1 account bound to device, false if not
     */
    protected static boolean doesDeviceHasAccounts(Activity context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccounts();
        if(accounts.length == 0) {
            Intent intent = AccountManager.newChooseAccountIntent(null, null,
                    new String[]{"com.google"}, false, null, null, null, null);

            context.startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
        }
        return accounts.length != 0;
    }

    /**
     * Cleans up given array from duplicate values.
     *
     * @param array Array of Strings wanted to be cleared
     * @return Array of unique Strings.
     */
    protected static String[] cleanFromDuplicates(String[] array) {
        List<String> filteredList = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            if (!filteredList.contains(array[i])) {
                filteredList.add(array[i]);
            }
        }
        String[] filteredArray = new String[filteredList.size()];
        for (int i = 0; i < filteredList.size(); i++) {
            filteredArray[i] = filteredList.get(i);
        }
        return filteredArray;
    }

    /**
     * Cleans up given list from duplicate values.
     *
     * @param list List of Strings wanted to be cleared
     * @return List of unique Strings.
     */
    protected static List<String> cleanFromDuplicates(List<String> list) {
        List<String> filteredList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (!filteredList.contains(list.get(i))) {
                filteredList.add(list.get(i));
            }
        }
        return filteredList;
    }
}
