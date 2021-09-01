/*
 * Copyright 2016-2020 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.apps.adrcotfas.goodtime.Settings;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.UpgradeDialogHelper;
import com.apps.adrcotfas.goodtime.Util.StringUtils;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.Util.TimePickerDialogFixedNougatSpinner;
import com.takisoft.preferencex.PreferenceFragmentCompat;
import com.takisoft.preferencex.RingtonePreferenceDialogFragmentCompat;
import com.takisoft.preferencex.RingtonePreference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import static com.apps.adrcotfas.goodtime.Settings.PreferenceHelper.DISABLE_SOUND_AND_VIBRATION;

import static com.apps.adrcotfas.goodtime.Settings.PreferenceHelper.DND_MODE;
import static com.apps.adrcotfas.goodtime.Settings.PreferenceHelper.VIBRATION_TYPE;
import static com.apps.adrcotfas.goodtime.Util.BatteryUtils.isIgnoringBatteryOptimizations;

public class SettingsFragment extends PreferenceFragmentCompat implements ActivityCompat.OnRequestPermissionsResultCallback, TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "SettingsFragment";
    private CheckBoxPreference mPrefDisableSoundCheckbox;
    private CheckBoxPreference mPrefDndMode;
    private SwitchPreferenceCompat mPrefReminder;

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        mPrefDisableSoundCheckbox = findPreference(DISABLE_SOUND_AND_VIBRATION);
        mPrefDndMode = findPreference(DND_MODE);
        setupReminderPreference();
    }

    private void setupReminderPreference() {
        mPrefReminder = findPreference(PreferenceHelper.ENABLE_REMINDER);
        mPrefReminder.setSummaryOn(StringUtils.formatTime(PreferenceHelper.getTimeOfReminder()));
        mPrefReminder.setSummaryOff("");
        mPrefReminder.setOnPreferenceClickListener(preference -> {
            mPrefReminder.setChecked(!mPrefReminder.isChecked());
            return true;
        });
        mPrefReminder.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((boolean)newValue) {
                final long millis = PreferenceHelper.getTimeOfReminder();
                final DateTime time = new DateTime(millis);

                TimePickerDialogFixedNougatSpinner d = new TimePickerDialogFixedNougatSpinner(
                        requireActivity(),
                        R.style.DialogTheme,
                        SettingsFragment.this,
                        time.getHourOfDay(),
                        time.getMinuteOfHour(),
                        DateFormat.is24HourFormat(getContext()));
                d.show();
                return true;
            }
            return false;
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setAlpha(0.f);
        view.animate().alpha(1.f).setDuration(100);
        return view;
    }

    @SuppressLint("BatteryLife")
    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.settings));

        setupTheme();
        setupRingtone();
        setupScreensaver();
        setupAutoStartSessionVsInsistentNotification();
        setupDisableSoundCheckBox();
        setupDnDCheckBox();
        setupFlashingNotificationPref();
        setupOneMinuteLeftNotificationPref();

        final Preference disableBatteryOptimizationPref = findPreference(PreferenceHelper.DISABLE_BATTERY_OPTIMIZATION);
        if (!isIgnoringBatteryOptimizations(requireContext())) {
            disableBatteryOptimizationPref.setVisible(true);
            disableBatteryOptimizationPref.setOnPreferenceClickListener(preference -> {

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                startActivity(intent);
                return true;
            });
        } else {
            disableBatteryOptimizationPref.setVisible(false);
        }

        findPreference(PreferenceHelper.DISABLE_WIFI).setVisible(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q);
        mPrefDndMode.setVisible(true);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof RingtonePreference) {
            try {
                RingtonePreferenceDialogFragmentCompat dialog =
                        RingtonePreferenceDialogFragmentCompat.newInstance(preference.getKey());
                dialog.setTargetFragment(this, 0);
                if (preference.getKey().equals(PreferenceHelper.RINGTONE_BREAK_FINISHED)
                        && !PreferenceHelper.isPro()) {
                    UpgradeDialogHelper.launchUpgradeDialog(requireActivity().getSupportFragmentManager());
                }
                else {
                    dialog.show(getParentFragmentManager(), null);
                }
            } catch (NumberFormatException e) {
                //TODO: handle this later
                Log.e(TAG, "The annoying RingtonePreferenceDialog exception was thrown");
                Toast.makeText(
                        requireActivity(),
                        "Something went wrong",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (preference.getKey().equals(PreferenceHelper.TIMER_STYLE)) {
            super.onDisplayPreferenceDialog(preference);
        } else if (preference.getKey().equals(VIBRATION_TYPE)) {
            VibrationPreferenceDialogFragment dialog = VibrationPreferenceDialogFragment.newInstance(preference.getKey());
            dialog.setTargetFragment(this, 0);
            dialog.show(getParentFragmentManager(), null);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    private void setupAutoStartSessionVsInsistentNotification() {
        // Continuous mode versus insistent notification
        CheckBoxPreference autoWork = findPreference(PreferenceHelper.AUTO_START_WORK);
        autoWork.setOnPreferenceChangeListener((preference, newValue) -> {
            final CheckBoxPreference pref = findPreference(PreferenceHelper.INSISTENT_RINGTONE);
            if ((boolean)newValue) {
                pref.setChecked(false);
            }
            return true;
        });

        CheckBoxPreference autoBreak = findPreference(PreferenceHelper.AUTO_START_BREAK);
        autoBreak.setOnPreferenceChangeListener((preference, newValue) -> {
            final CheckBoxPreference pref = findPreference(PreferenceHelper.INSISTENT_RINGTONE);
            if ((boolean)newValue) {
                pref.setChecked(false);
            }
            return true;
        });

        final CheckBoxPreference insistentRingPref = findPreference(PreferenceHelper.INSISTENT_RINGTONE);
        insistentRingPref.setOnPreferenceClickListener(PreferenceHelper.isPro() ? null : preference -> {
            UpgradeDialogHelper.launchUpgradeDialog(requireActivity().getSupportFragmentManager());
            insistentRingPref.setChecked(false);
            return true;
        });
        insistentRingPref.setOnPreferenceChangeListener(PreferenceHelper.isPro() ? (preference, newValue) -> {
            final CheckBoxPreference p1 = findPreference(PreferenceHelper.AUTO_START_BREAK);
            final CheckBoxPreference p2 = findPreference(PreferenceHelper.AUTO_START_WORK);
            if ((boolean)newValue) {
                p1.setChecked(false);
                p2.setChecked(false);
            }
            return true;
        } : null);
    }

    private void setupRingtone() {
        final RingtonePreference prefWork = findPreference(PreferenceHelper.RINGTONE_WORK_FINISHED);
        final RingtonePreference prefBreak = findPreference(PreferenceHelper.RINGTONE_BREAK_FINISHED);

        if (PreferenceHelper.isPro()) {
            prefWork.setOnPreferenceChangeListener(null);
        } else {
            prefBreak.setRingtone(prefWork.getRingtone());
            prefWork.setOnPreferenceChangeListener((preference, newValue) -> {
                prefBreak.setRingtone((Uri) newValue);
                prefBreak.setSummary(prefBreak.getSummary());
                return true;
            });
        }

        final SwitchPreferenceCompat prefEnableRingtone = findPreference(PreferenceHelper.ENABLE_RINGTONE);
        toggleEnableRingtonePreference(prefEnableRingtone.isChecked());
        prefEnableRingtone.setOnPreferenceChangeListener((preference, newValue) -> {
            toggleEnableRingtonePreference((Boolean) newValue);
            return true;
        });
    }

    private void setupScreensaver() {
        final CheckBoxPreference screensaverPref = SettingsFragment.this.findPreference(PreferenceHelper.ENABLE_SCREENSAVER_MODE);
        findPreference(PreferenceHelper.ENABLE_SCREENSAVER_MODE).setOnPreferenceClickListener(PreferenceHelper.isPro() ? null : preference -> {
            UpgradeDialogHelper.launchUpgradeDialog(requireActivity().getSupportFragmentManager());
            screensaverPref.setChecked(false);
            return true;
        });

        findPreference(PreferenceHelper.ENABLE_SCREEN_ON).setOnPreferenceChangeListener((preference, newValue) -> {
            if (!((boolean) newValue)) {
                if (screensaverPref.isChecked()) {
                    screensaverPref.setChecked(false);
                }
            }
            return true;
        });
    }

    private void setupTheme() {
        SwitchPreferenceCompat prefAmoled = findPreference(PreferenceHelper.AMOLED);
        prefAmoled.setOnPreferenceClickListener(PreferenceHelper.isPro() ? null : preference -> {
            UpgradeDialogHelper.launchUpgradeDialog(requireActivity().getSupportFragmentManager());
            prefAmoled.setChecked(true);
            return true;
        });
        prefAmoled.setOnPreferenceChangeListener(PreferenceHelper.isPro() ? (preference, newValue) -> {
            ThemeHelper.setTheme((SettingsActivity)getActivity());
            getActivity().recreate();

            return true;
        } : null);
    }

    private void updateDisableSoundCheckBoxSummary(CheckBoxPreference pref, boolean notificationPolicyAccessGranted) {
        if (notificationPolicyAccessGranted) {
            pref.setSummary("");
        } else {
            pref.setSummary(R.string.settings_grant_permission);
        }
    }

    private void setupDisableSoundCheckBox() {
        if (isNotificationPolicyAccessDenied()) {
            updateDisableSoundCheckBoxSummary(mPrefDisableSoundCheckbox,false);
            mPrefDisableSoundCheckbox.setChecked(false);
            mPrefDisableSoundCheckbox.setOnPreferenceClickListener(
                    preference -> {
                        requestNotificationPolicyAccess();
                        return false;
                    }
            );
        } else {
            updateDisableSoundCheckBoxSummary(mPrefDisableSoundCheckbox, true);
            mPrefDisableSoundCheckbox.setOnPreferenceClickListener(preference -> {
                if (mPrefDndMode.isChecked()) {
                    mPrefDndMode.setChecked(false);
                    return true;
                }
                return false;
            });
        }
    }

    private void setupFlashingNotificationPref() {
        SwitchPreferenceCompat pref = findPreference(PreferenceHelper.ENABLE_FLASHING_NOTIFICATION);
        pref.setOnPreferenceClickListener(PreferenceHelper.isPro() ? null : preference -> {
            UpgradeDialogHelper.launchUpgradeDialog(requireActivity().getSupportFragmentManager());
            pref.setChecked(false);
            return true;
        });
    }

    private void setupOneMinuteLeftNotificationPref() {
        SwitchPreferenceCompat pref = findPreference(PreferenceHelper.ENABLE_ONE_MINUTE_BEFORE_NOTIFICATION);
        pref.setOnPreferenceClickListener(PreferenceHelper.isPro() ? null : preference -> {
            UpgradeDialogHelper.launchUpgradeDialog(requireActivity().getSupportFragmentManager());
            pref.setChecked(false);
            return true;
        });
    }

    private void setupDnDCheckBox() {
        if (isNotificationPolicyAccessDenied()) {
            updateDisableSoundCheckBoxSummary(mPrefDndMode, false);
            mPrefDndMode.setChecked(false);
            mPrefDndMode.setOnPreferenceClickListener(
                    preference -> {
                        requestNotificationPolicyAccess();
                        return false;
                    }
            );
        } else {
            updateDisableSoundCheckBoxSummary(mPrefDndMode, true);
            mPrefDndMode.setOnPreferenceClickListener(preference -> {
                if (mPrefDisableSoundCheckbox.isChecked()) {
                    mPrefDisableSoundCheckbox.setChecked(false);
                    return true;
                }
                return false;
            });
        }
    }

    private void requestNotificationPolicyAccess() {
        if (isNotificationPolicyAccessDenied()) {
            Intent intent = new Intent(android.provider.Settings.
                    ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isNotificationPolicyAccessDenied() {
        NotificationManager notificationManager = (NotificationManager)
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        return !notificationManager.isNotificationPolicyAccessGranted();
    }

    private void toggleEnableRingtonePreference(Boolean newValue) {
        findPreference(PreferenceHelper.RINGTONE_WORK_FINISHED).setVisible(newValue);
        findPreference(PreferenceHelper.RINGTONE_BREAK_FINISHED).setVisible(newValue);
        findPreference(PreferenceHelper.PRIORITY_ALARM).setVisible(newValue);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        final long millis = new LocalTime(hourOfDay, minute).toDateTimeToday().getMillis();
        PreferenceHelper.setTimeOfReminder(millis);
        mPrefReminder.setSummaryOn(StringUtils.formatTime(millis));
        mPrefReminder.setChecked(true);
    }
}

