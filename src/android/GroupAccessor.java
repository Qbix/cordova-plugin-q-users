package com.q.users.cordova.plugin;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import org.apache.cordova.CordovaInterface;

import com.q.users.cordova.plugin.AccNameGroup;
import com.q.users.cordova.plugin.QbixGroup;
import com.q.users.cordova.plugin.RawIdLabelId;
import com.q.users.cordova.plugin.GroupHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class defines SDK-independent API for communication with
 * Contacts Provider.
 */
public class GroupAccessor {

    private CordovaInterface app;

    protected GroupAccessor(CordovaInterface context) {
        this.app = context;
    }

    /**
     * Gets all available groups for users.
     *
     * @return list of {@link QbixGroup} POJO
     */
    protected List<QbixGroup> getAllLabels() {
        List<QbixGroup> labels = new ArrayList<>();
        //Get all labels (including not visible and deleted ones) from content provider
        Cursor cursor = app.getActivity().getContentResolver().query(
                ContactsContract.Groups.CONTENT_SUMMARY_URI,
                new String[]{
                        ContactsContract.Groups._ID,
                        ContactsContract.Groups.SOURCE_ID,
                        ContactsContract.Groups.TITLE,
                        ContactsContract.Groups.NOTES,
                        ContactsContract.Groups.SUMMARY_COUNT,
                        ContactsContract.Groups.GROUP_VISIBLE,
                        ContactsContract.Groups.DELETED,
                        ContactsContract.Groups.SHOULD_SYNC,
                        ContactsContract.Groups.GROUP_IS_READ_ONLY
                },
                null,
                null,
                null);
        List<String> sourceIds = new ArrayList<>();
        List<RawIdLabelId> rawIdLabelIds = GroupHelper.getExistingRawIdLabelIdPairs(app.getActivity());
        HashMap<String, String> rawIdContactIdPair = GroupHelper.getExistingRawIdContactIdPairs(app.getActivity());
        while (cursor.moveToNext()) {
            QbixGroup group = new QbixGroup();

            group.sourceId = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.SOURCE_ID));
            group.title = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.TITLE));
            group.notes = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.NOTES));
            group.summaryCount = cursor.getInt(cursor.getColumnIndex(ContactsContract.Groups.SUMMARY_COUNT));
            group.isVisible = cursor.getInt(cursor.getColumnIndex(ContactsContract.Groups.GROUP_VISIBLE)) == 0;
            group.isDeleted = cursor.getInt(cursor.getColumnIndex(ContactsContract.Groups.DELETED)) == 1;
            group.shouldSync = cursor.getInt(cursor.getColumnIndex(ContactsContract.Groups.SHOULD_SYNC)) == 1;
            group.readOnly = cursor.getInt(cursor.getColumnIndex(ContactsContract.Groups.GROUP_IS_READ_ONLY)) == 1;
            Log.i("group_info_checker", "id: " + cursor.getString(cursor.getColumnIndex(ContactsContract.Groups._ID)));
            Log.i("group_info_checker", "source_id: " + group.sourceId);
            Log.i("group_info_checker", "title: " + group.title);
            Log.i("group_info_checker", "notes: " + group.notes);
            Log.i("group_info_checker", "summary_count: " + group.summaryCount);
            Log.i("group_info_checker", "is_visible: " + group.isVisible);
            Log.i("group_info_checker", "deleted: " + group.isDeleted);
            Log.i("group_info_checker", "should_sync: " + group.shouldSync);
            Log.i("group_info_checker", "read_only: " + group.readOnly);
            String labelId = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups._ID));
            List<Integer> rawIds = new ArrayList<>();
            for (int i = 0; i < rawIdLabelIds.size(); i++) {
                if (rawIdLabelIds.get(i).labelId.equals(labelId)) {
                    rawIds.add(Integer.valueOf(rawIdLabelIds.get(i).rawId));
                }
            }
            List<Integer> contactIds = GroupHelper.getContactIds(rawIdContactIdPair, rawIds);
            if (group.sourceId != null) {
                if (!sourceIds.contains(cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.SOURCE_ID)))) {
                    group.contactIds = contactIds;
                    sourceIds.add(cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.SOURCE_ID)));
                    labels.add(group);
                } else {
                    int index = sourceIds.indexOf(cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.SOURCE_ID)));
                    labels.get(index).contactIds.addAll(contactIds);
                    Log.i("group_info_checker", "group: " + cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.SOURCE_ID)) + " is existing");
                }
            }

        }
        cursor.close();
        return labels;
    }

    /**
     * Removes label from contacts.
     *
     * @param sourceId   The source id which label wanted to be removed
     * @param contactIds Array of contact ids from which label must be removed
     * @return success message if succeeded and exception message if failed
     */
    protected String removeLabelFromContacts(String sourceId, String[] contactIds) {
        ArrayList<ContentProviderOperation> ops =
                new ArrayList<>();
        String[] rawContactIds = GroupHelper.getRawContactIds(app.getActivity(), contactIds);
        HashMap<String, String> rawIdAccName = GroupHelper.getRawContactIdAccountNamePair(app.getActivity(), rawContactIds);
        HashMap<String, String> accNameLabelId = GroupHelper.getAccountNameLabelIdPair(app.getActivity(), sourceId);
        List<RawIdLabelId> existingLabels = GroupHelper.getExistingRawIdLabelIdPairs(app.getActivity(), rawContactIds);

        for (int i = 0; i < rawContactIds.length; i++) {
            String labelId = accNameLabelId.get(rawIdAccName.get(rawContactIds[i]));
            if (labelId != null) {
                for (int j = 0; j < existingLabels.size(); j++) {
                    if (existingLabels.get(j).rawId.equals(rawContactIds[i]) && existingLabels.get(j).labelId.equals(labelId)) {
                        ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                                .withSelection(ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
                                                + "' AND " + ContactsContract.Data.DATA1 + "='" + labelId
                                                + "' AND " + ContactsContract.Data.RAW_CONTACT_ID + "='" + rawContactIds[i] + "'",
                                        null)
                                .withYieldAllowed(i == rawContactIds.length - 1)
                                .build());
                    } else {
                        Log.d("delete_checker", "not that one!!! " + rawContactIds[i]);
                    }
                }
            } else {
                Log.d("delete_checker", "no label for that contact" + rawContactIds[i]);
            }
        }

        try {
            app.getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
            return e.getMessage();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return QUsersCordova.SUCCESS;
    }

    /**
     * Add label to given contacts.
     *
     * @param sourceId   source id which label wanted to be added
     * @param contactIds Contact id's array to which label should be added
     * @return success message if succeeded and exception message if failed
     */
    protected String addLabelToContacts(String sourceId, String[] contactIds) {
        ArrayList<ContentProviderOperation> ops =
                new ArrayList<>();
        String[] rawContactIds = GroupHelper.getRawContactIds(app.getActivity(), contactIds);
        HashMap<String, String> rawIdAccName = GroupHelper.getRawContactIdAccountNamePair(app.getActivity(), rawContactIds);
        HashMap<String, String> accNameLabelId = GroupHelper.getAccountNameLabelIdPair(app.getActivity(), sourceId);
        List<RawIdLabelId> existingLabels = GroupHelper.getExistingRawIdLabelIdPairs(app.getActivity(), rawContactIds);
        for (int i = 0; i < rawContactIds.length; i++) {
            String labelId = accNameLabelId.get(rawIdAccName.get(rawContactIds[i]));
            boolean exists = false;
            if (labelId != null) {
                for (int j = 0; j < existingLabels.size(); j++) {
                    if (existingLabels.get(j).rawId.equals(rawContactIds[i]) && existingLabels.get(j).labelId.equals(labelId)) {
                        exists = true;
                    }
                }
                if (!exists) {
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactIds[i])
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.Data.DATA1, accNameLabelId.get(rawIdAccName.get(rawContactIds[i])))
                            .withYieldAllowed(i == contactIds.length - 1)
                            .build());
                } else {
                    Log.d("duplicate_checker", "duplicate!!! " + rawContactIds[i]);
                }

            } else {
                Log.d("duplicate_checker", "no label for that contact" + rawContactIds[i]);
            }
        }

        try {
            ContentProviderResult[] results = app.getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            if (results.length >= 1) {
                Log.d("duplicate_checker", results[0].toString());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return e.getMessage();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return QUsersCordova.SUCCESS;
    }

    /**
     * Removes label from database and syncs all accounts.
     *
     * @param sourceId Source id that is wanted to be deleted
     * @return success message if succeed and exception message if failed
     */
    protected String removeLabelFromData(String sourceId) {
        ArrayList<ContentProviderOperation> ops =
                new ArrayList<>();

        ops.add(ContentProviderOperation.newDelete(ContactsContract.Groups.CONTENT_URI)
                .withSelection(ContactsContract.Groups.SOURCE_ID + "=?", new String[]{sourceId})
                .withYieldAllowed(true)
                .build());
        try {
            ContentProviderResult[] result = app.getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Log.d("delete_checker", "removeLabelFromData: " + result.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
            return e.getMessage();
        } catch (OperationApplicationException e) {
            Log.d("error_tag", e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
        GroupHelper.requestSyncNow(app.getActivity());
        return QUsersCordova.SUCCESS;
    }

    /**
     * Gets labels that have given sourceIds.
     *
     * @param sourceIds Source ids list which labels wanted to be returned.
     * @return list of {@link QbixGroup} POJO
     */
    protected List<QbixGroup> getLabelsBySourceId(String[] sourceIds) {
        Cursor groupCursor = app.getActivity().getContentResolver().query(ContactsContract.Groups.CONTENT_SUMMARY_URI,
                new String[]{
                        ContactsContract.Groups.SOURCE_ID,
                        ContactsContract.Groups.TITLE,
                        ContactsContract.Groups.ACCOUNT_NAME,
                        ContactsContract.Groups.NOTES,
                        ContactsContract.Groups.SUMMARY_COUNT,
                        ContactsContract.Groups.GROUP_VISIBLE,
                        ContactsContract.Groups.DELETED,
                        ContactsContract.Groups.SHOULD_SYNC,
                        ContactsContract.Groups.GROUP_IS_READ_ONLY
                },
                ContactsContract.Groups.SOURCE_ID + GroupHelper.getSuffix(sourceIds.length),
                sourceIds,
                null);
        AccountManager accountManager = AccountManager.get(app.getActivity());
        Account[] accounts = accountManager.getAccounts();
        List<AccNameGroup> accNameGroups = new ArrayList<>();
        HashMap<String, String> rawIdContactIdPair = GroupHelper.getExistingRawIdContactIdPairs(app.getActivity());
        while (groupCursor.moveToNext()) {
            AccNameGroup group = new AccNameGroup();
            group.sourceId = groupCursor.getString(groupCursor.getColumnIndex(ContactsContract.Groups.SOURCE_ID));
            group.title = groupCursor.getString(groupCursor.getColumnIndex(ContactsContract.Groups.TITLE));
            group.accountName = groupCursor.getString(groupCursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME));
            group.summaryCount = groupCursor.getInt(groupCursor.getColumnIndex(ContactsContract.Groups.SUMMARY_COUNT));
            group.notes = groupCursor.getString(groupCursor.getColumnIndex(ContactsContract.Groups.NOTES));
            group.isVisible = groupCursor.getInt(groupCursor.getColumnIndex(ContactsContract.Groups.GROUP_VISIBLE)) == 0;
            group.isDeleted = groupCursor.getInt(groupCursor.getColumnIndex(ContactsContract.Groups.DELETED)) == 1;
            group.shouldSync = groupCursor.getInt(groupCursor.getColumnIndex(ContactsContract.Groups.SHOULD_SYNC)) == 1;
            group.readOnly = groupCursor.getInt(groupCursor.getColumnIndex(ContactsContract.Groups.GROUP_IS_READ_ONLY)) == 1;
            if (group.sourceId != null) {
                accNameGroups.add(group);
            }
        }
        groupCursor.close();
        List<String> uniqueSourceId = new ArrayList<>();
        List<QbixGroup> finalGroups = new ArrayList<>();
        for (int i = 0; i < accNameGroups.size(); i++) {
            AccNameGroup accNameGroup = accNameGroups.get(i);
            if (!uniqueSourceId.contains(accNameGroup.sourceId)) {
                GetRealGroup:
                {
                    for (int j = 0; j < accounts.length; j++) {
                        if (accNameGroup.accountName.equals(accounts[j].name)) {
                            for (int k = 0; k < accNameGroups.size(); k++) {
                                AccNameGroup realGroup = accNameGroups.get(k);
                                if (realGroup.sourceId.equals(accNameGroup.sourceId) && realGroup.accountName.equals(accNameGroup.accountName)) {
                                    realGroup.contactIds = GroupHelper.getContactIds(rawIdContactIdPair,
                                            GroupHelper.getRawIdsBySourceId(app.getActivity(), realGroup.sourceId));
                                    finalGroups.add(realGroup);
                                    uniqueSourceId.add(realGroup.sourceId);
                                    break GetRealGroup;
                                }
                            }
                        }
                    }
                }
            }
        }

        return finalGroups;
    }

    /**
     * Adds new label with given title on every account on device and syncs them all
     * (for generating some autogenerating info).
     *
     * @param title Title of label wanted to be added.
     * @return success message if succeed and exception message if failed
     */
    protected String addLabelToDatabase(String title) {
        ArrayList<ContentProviderOperation> ops =
                new ArrayList<>();
        AccountManager accountManager = AccountManager.get(app.getActivity());
        Account[] accounts = accountManager.getAccounts();
        for (int i = 0; i < accounts.length; i++) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Groups.CONTENT_URI)
                    .withValue(ContactsContract.Groups.TITLE, title)
                    .withValue(ContactsContract.Groups.ACCOUNT_NAME, accounts[i].name)
                    .withValue(ContactsContract.Groups.ACCOUNT_TYPE, accounts[i].type)
                    .withYieldAllowed(true)
                    .build()
            );
        }

        try {
            ContentProviderResult[] results = app.getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            if (results.length >= 1) {
                //Syncs accounts for generate SOURCE_ID, SYNC2, SYNC3
                GroupHelper.requestSyncNow(app.getActivity());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return e.getMessage();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return QUsersCordova.SUCCESS;
    }

    /**
     * Changes existing labels title into given one.
     *
     * @param sourceId Existing label's sourceId which title wanted to be changed
     * @param title    New title
     * @return success message if succeed and exception message if failed
     */
    protected String editLabelInDatabase(String sourceId, String title) {
        ArrayList<ContentProviderOperation> ops =
                new ArrayList<>();
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Groups.CONTENT_URI)
                .withSelection(ContactsContract.Groups.SOURCE_ID + "=?", new String[]{sourceId})
                .withValue(ContactsContract.Groups.TITLE, title)
                .withYieldAllowed(true)
                .build()
        );

        try {
            ContentProviderResult[] results = app.getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
            return e.getMessage();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return QUsersCordova.SUCCESS;
    }

    /**
     * Sets to contact given labels list.(if list is empty, then removes all labels from contact)
     *
     * @param contactId The contact id from which must be removed all labels.
     * @param sourceIds List of sourceIds which labels wanted to be added.
     * @return success message if succeed and exception message if failed
     */
    protected String setLabelListForContact(String contactId, List<String> sourceIds) {
        String[] rawIds = GroupHelper.getRawContactIds(app.getActivity(), new String[]{contactId});
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        List<RawIdLabelId> existingLabels = GroupHelper.getExistingRawIdLabelIdPairs(app.getActivity(), rawIds);
        List<String> systemLabelIds = GroupHelper.getSystemIds(app.getActivity());
        String accountName = GroupHelper.getRawContactIdAccountName(app.getActivity(), rawIds[0]);
        for (int i = 0; i < rawIds.length; i++) {
            for (int j = 0; j < existingLabels.size(); j++) {
                if (existingLabels.get(j).rawId.equals(rawIds[i]) && !systemLabelIds.contains(existingLabels.get(j).labelId)) {
                    ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                            .withSelection(ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
                                            + "' AND " + ContactsContract.Data.DATA1 + "='" + existingLabels.get(j).labelId
                                            + "' AND " + ContactsContract.Data.RAW_CONTACT_ID + "='" + rawIds[i] + "'",
                                    null)
                            .withYieldAllowed(i == rawIds.length - 1)
                            .build());
                }
            }
        }

        if (!sourceIds.isEmpty()) {
            for (int i = 0; i < sourceIds.size(); i++) {
                HashMap<String, String> accNameLabelIdPair = GroupHelper.getAccountNameLabelIdPair(app.getActivity(), sourceIds.get(i));
                for (int j = 0; j < rawIds.length; j++) {
                    if (!systemLabelIds.contains(accNameLabelIdPair.get(accountName))) {
                        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawIds[j])
                                .withValue(ContactsContract.Data.DATA1, accNameLabelIdPair.get(accountName))
                                .withYieldAllowed(i == sourceIds.size() - 1)
                                .build());
                    }
                }
            }
        }

        try {
            ContentProviderResult[] results = app.getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
            return e.getMessage();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return QUsersCordova.SUCCESS;
    }

    /**
     * Gets labels of given contact ids.
     *
     * @param contactIds Contacts' ids which labels wanted to be returned
     * @param doUnion    union by labelIds if true, otherwise intersection
     * @return list of {@link QbixGroup} POJO
     */
    protected List<QbixGroup> getLabelsByContactIds(List<String> contactIds, boolean doUnion) {
        String[] contactIdArray = new String[contactIds.size()];
        for (int i = 0; i < contactIds.size(); i++) {
            contactIdArray[i] = contactIds.get(i);
        }
        String[] rawIds = GroupHelper.getRawContactIds(app.getActivity(), contactIdArray);
        String[] sourceIds;
        if (doUnion) {
            sourceIds = GroupHelper.getUnionSourceIds(app.getActivity(), rawIds);
        } else {
            sourceIds = GroupHelper.getNotUnionSourceIds(app.getActivity(), rawIds);
        }
        List<QbixGroup> groupList = getLabelsBySourceId(sourceIds);
        return groupList;
    }

    /**
     * Gets contact ids depending on given smart name.
     *
     * @param name smart name
     *             (See description of names
     *             {@link QUsersCordova#UNCATEGORIZED_SMART_NAME},
     *             {@link QUsersCordova#BY_LAST_TIME_UPDATED_SMART_NAME},
     *             {@link QUsersCordova#BY_COMPANY_SMART_NAME},
     *             {@link QUsersCordova#HAS_EMAIL_SMART_NAME},
     *             {@link QUsersCordova#HAS_PHONE_SMART_NAME},
     *             {@link QUsersCordova#HAS_PHOTO_SMART_NAME})
     * @return Array of contact ids
     */
    protected String[] getContactList(String name) {
        String[] contactIds = new String[0];
        switch (name) {
            case QUsersCordova.UNCATEGORIZED_SMART_NAME:
                contactIds = GroupHelper.smartUncategorized(app.getActivity());
                break;
            case QUsersCordova.BY_LAST_TIME_UPDATED_SMART_NAME:
                contactIds = GroupHelper.smartByTimeUpdated(app.getActivity());
                break;
            case QUsersCordova.BY_COMPANY_SMART_NAME:
                contactIds = GroupHelper.smartByCompany(app.getActivity());
                break;
            case QUsersCordova.HAS_EMAIL_SMART_NAME:
                contactIds = GroupHelper.smartHasEmail(app.getActivity());
                break;
            case QUsersCordova.HAS_PHONE_SMART_NAME:
                contactIds = GroupHelper.smartHasPhone(app.getActivity());
                break;
            case QUsersCordova.HAS_PHOTO_SMART_NAME:
                contactIds = GroupHelper.smartHasPhoto(app.getActivity());
                break;
        }
        return contactIds;
    }
}
