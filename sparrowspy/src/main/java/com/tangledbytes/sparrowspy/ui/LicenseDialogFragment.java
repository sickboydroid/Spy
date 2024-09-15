package com.tangledbytes.sparrowspy.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.DialogFragment;

import com.tangledbytes.sparrowspy.R;
import com.tangledbytes.sparrowspy.events.SparrowActions;
import com.tangledbytes.sparrowspy.utils.Constants;
import com.tangledbytes.sparrowspy.utils.Resources;

import java.io.IOException;
import java.io.InputStream;

public class LicenseDialogFragment extends DialogFragment {
    private static final String TAG = "LicenseDialogFragment";
    private Button acceptButton;
    private Button rejectButton;
    private TextView licenseTextView;
    private ScrollView scrollView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("License Agreement");

        // Inflate the custom view for the dialog
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_license_dialog, null);
        builder.setView(view);

        // Initialize views
        acceptButton = view.findViewById(R.id.accept_button);
        rejectButton = view.findViewById(R.id.reject_button);
        licenseTextView = view.findViewById(R.id.license_textview);
        scrollView = view.findViewById(R.id.scroll_view);

        // Disable buttons initially
        acceptButton.setEnabled(false);
        rejectButton.setEnabled(false);

        // Load the license text from assets/license.html
        loadLicenseHtml();

        // Enable buttons only after scrolling to the bottom
        scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (!scrollView.canScrollVertically(1)) {
                acceptButton.setEnabled(true);
                rejectButton.setEnabled(true);
            }
        });

        // Set button click listeners
        acceptButton.setOnClickListener(v -> acceptLicense());
        rejectButton.setOnClickListener(v -> rejectLicense());

        return builder.create();
    }

    private void loadLicenseHtml() {
        try {
            InputStream inputStream = getActivity().getAssets().open("license.html");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            String html = new String(buffer);
            html = html.replaceAll("\\{\\{APP_NAME\\}\\}", Resources.Strings.appName);
            licenseTextView.setText(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT));
        } catch (IOException e) {
            Log.wtf(TAG, "Exception occurred while loading 'license.html' from assets", e);
        }
    }

    private void acceptLicense() {
        // Save the preference so the dialog doesn't show again
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(Constants.LICENSE_ACCEPTED_KEY, true).apply();
        getActivity().sendBroadcast(new Intent(SparrowActions.ACTION_LICENSE_ACCEPTED));
        // Close the dialog
        dismiss();
    }

    private void rejectLicense() {
        // Close the app
        getActivity().finish();
    }
}
