package com.apps.adrcotfas.goodtime.settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.apps.adrcotfas.goodtime.R;

public class ProperSeekBarPreferenceDialog extends PreferenceDialogFragmentCompat {

    interface Listener {
        void onValueSet();
    }

    private Listener listener;

    public static ProperSeekBarPreferenceDialog newInstance(String key, Listener listener) {
        ProperSeekBarPreferenceDialog fragment = new ProperSeekBarPreferenceDialog();
        Bundle b = new Bundle(1);
        b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
        fragment.setArguments(b);
        fragment.listener = listener;
        return fragment;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        ProperSeekBarPreference seekBarPreference = (ProperSeekBarPreference)getPreference();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        @SuppressLint("InflateParams")
        View dialogView = inflater.inflate(R.layout.dialog_set_seekbar_value, null);
        builder.setView(dialogView);

        builder.setTitle(seekBarPreference.getTitle());
        builder.setPositiveButton(android.R.string.ok, (di, i) -> {
            final EditText input = dialogView.findViewById(R.id.value);
            final String value = input.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                final int seekBarValue = Integer.parseInt(value);
                if (seekBarPreference.getValue() != seekBarValue) {
                    listener.onValueSet();
                    seekBarPreference.setValue(seekBarValue);
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, (di, i) -> {
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        // do nothing
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return d;
    }
}
