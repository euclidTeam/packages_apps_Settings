/*
 * Copyright (C) 2025 AxionOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.display

import android.content.ContentResolver
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.Toast
import androidx.preference.Preference
import com.android.settingslib.widget.MainSwitchPreference
import com.android.settings.R
import com.prism.settings.preferences.RadioButtonPreference
import com.prism.settings.preferences.BasePreferenceFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.*

class AmbientSettings : BasePreferenceFragment(R.xml.ambient),
    RadioButtonPreference.OnRadioButtonClickedListener {

    private lateinit var mainSwitch: MainSwitchPreference
    private lateinit var alwaysPref: RadioButtonPreference
    private lateinit var schedulePref: RadioButtonPreference
    private lateinit var chargingPref: RadioButtonPreference
    private lateinit var startTimePref: Preference
    private lateinit var endTimePref: Preference

    private lateinit var cr: ContentResolver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cr = requireContext().contentResolver

        mainSwitch = findPreference(KEY_DOZE_MAIN_SWITCH)!!
        alwaysPref = findPreference(KEY_DOZE_ALWAYS_ON)!!
        schedulePref = findPreference(KEY_DOZE_SCHEDULE)!!
        chargingPref = findPreference(KEY_DOZE_CHARGING)!!
        startTimePref = findPreference(KEY_DOZE_START_TIME)!!
        endTimePref = findPreference(KEY_DOZE_END_TIME)!!

        initMainSwitch()
        initRadioPreferences()
        initStartEndTimePreferences()
    }

    private fun initMainSwitch() {
        mainSwitch.isChecked = isSettingEnabled(Settings.Secure.DOZE_ALWAYS_ON)
        mainSwitch.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                onRadioButtonClicked(alwaysPref)
            } else {
                disableAllModes()
            }
            true
        }
    }

    private fun initRadioPreferences() {
        val alwaysOn = isSettingEnabled(Settings.Secure.DOZE_ALWAYS_ON)
        val scheduleEnabled = isSettingEnabled(KEY_SCHEDULE)
        val chargingEnabled = isSettingEnabled(KEY_CHARGING)

        alwaysPref.isSelected = alwaysOn && !scheduleEnabled && !chargingEnabled
        schedulePref.isSelected = scheduleEnabled
        chargingPref.isSelected = chargingEnabled

        alwaysPref.setOnRadioButtonClickedListener(this)
        schedulePref.setOnRadioButtonClickedListener(this)
        chargingPref.setOnRadioButtonClickedListener(this)

        refreshRadios()
    }

    override fun onRadioButtonClicked(pref: RadioButtonPreference) {
        disableAllModes()

        when (pref) {
            alwaysPref -> {
                setSetting(Settings.Secure.DOZE_ALWAYS_ON, true)
                setSetting(KEY_USER_ENABLED, true)
            }
            schedulePref -> {
                val (sh, sm, eh, em) = getScheduleTimes() ?: return showInvalidScheduleToast()
                if (isTimeRangeValid(sh, sm, eh, em)) {
                    setSetting(KEY_SCHEDULE, true)
                } else {
                    return showInvalidScheduleToast()
                }
            }
            chargingPref -> setSetting(KEY_CHARGING, true)
        }

        refreshRadios()
    }

    private fun refreshRadios() {
        val alwaysOn = isSettingEnabled(Settings.Secure.DOZE_ALWAYS_ON)
        val scheduleEnabled = isSettingEnabled(KEY_SCHEDULE)
        val chargingEnabled = isSettingEnabled(KEY_CHARGING)

        alwaysPref.isSelected = alwaysOn && !scheduleEnabled && !chargingEnabled
        schedulePref.isSelected = scheduleEnabled
        chargingPref.isSelected = chargingEnabled

        updateStartEndTimeSummaries()
    }

    private fun initStartEndTimePreferences() {
        updateStartEndTimeSummaries()

        startTimePref.setOnPreferenceClickListener {
            showTimePicker(true)
            true
        }

        endTimePref.setOnPreferenceClickListener {
            showTimePicker(false)
            true
        }
    }

    private fun updateStartEndTimeSummaries() {
        val (start, end) = getScheduleRange() ?: return

        startTimePref.summary = start
        endTimePref.summary = end

        val (sh, sm, eh, em) = getScheduleTimes() ?: return

        val valid = isTimeRangeValid(sh, sm, eh, em)
        schedulePref.isEnabled = valid

        if (!valid) {
            val warning = getString(R.string.ambient_display_schedule_time_invalid_summary)
            startTimePref.summary = warning
            endTimePref.summary = warning
        }

        val visible = isSettingEnabled(KEY_SCHEDULE)
        startTimePref.isVisible = visible
        endTimePref.isVisible = visible
    }

    private fun showTimePicker(isStart: Boolean) {
        val (start, end) = getScheduleRange() ?: return
        val timeStr = if (isStart) start else end
        val (hour, minute) = timeStr.split(":").map { it.toIntOrNull() ?: 0 }

        val is24Hour = DateFormat.is24HourFormat(requireContext())
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(if (is24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText(if (isStart) R.string.ambient_display_start_time else R.string.ambient_display_end_time)
            .setTheme(R.style.Material3TimePicker)
            .build()

        picker.addOnPositiveButtonClickListener {
            val newTime = formatTime(picker.hour, picker.minute)
            val updatedRange = if (isStart) "$newTime-$end" else "$start-$newTime"
            Settings.Secure.putString(cr, KEY_TIME, updatedRange)

            val (sh, sm, eh, em) = getScheduleTimes() ?: return@addOnPositiveButtonClickListener
            if (!isTimeRangeValid(sh, sm, eh, em)) {
                setSetting(KEY_SCHEDULE, false)
            }

            updateStartEndTimeSummaries()
            refreshRadios()
        }

        picker.show(childFragmentManager, "material_time_picker")
    }

    private fun getScheduleRange(): Pair<String, String>? {
        val timeRange = Settings.Secure.getString(cr, KEY_TIME) ?: DEFAULT_TIME
        val parts = timeRange.split("-")
        return if (parts.size == 2) parts[0] to parts[1] else null
    }

    private fun getScheduleTimes(): List<Int>? {
        val (start, end) = getScheduleRange() ?: return null
        val (sh, sm) = start.split(":").mapNotNull { it.toIntOrNull() }
        val (eh, em) = end.split(":").mapNotNull { it.toIntOrNull() }
        return listOf(sh, sm, eh, em).takeIf { it.size == 4 }
    }

    private fun showInvalidScheduleToast() {
        Toast.makeText(requireContext(), R.string.ambient_display_schedule_time_invalid_summary, Toast.LENGTH_LONG).show()
    }

    private fun formatTime(hour: Int, minute: Int) = String.format(Locale.US, "%02d:%02d", hour, minute)

    private fun isTimeRangeValid(startHour: Int, startMin: Int, endHour: Int, endMin: Int): Boolean {
        val start = startHour * 60 + startMin
        val end = endHour * 60 + endMin
        return start != end
    }

    private fun isSettingEnabled(key: String) = Settings.Secure.getInt(cr, key, 0) == 1

    private fun setSetting(key: String, enabled: Boolean) {
        Settings.Secure.putInt(cr, key, if (enabled) 1 else 0)
    }

    private fun disableAllModes() {
        setSetting(Settings.Secure.DOZE_ALWAYS_ON, false)
        setSetting(KEY_SCHEDULE, false)
        setSetting(KEY_CHARGING, false)
        setSetting(KEY_USER_ENABLED, false)
    }

    companion object {
        private const val KEY_DOZE_MAIN_SWITCH = "doze_main_switch"
        private const val KEY_DOZE_ALWAYS_ON = "doze_always_on"
        private const val KEY_DOZE_SCHEDULE = "doze_schedule"
        private const val KEY_DOZE_CHARGING = "doze_charging"
        private const val KEY_DOZE_START_TIME = "doze_start_time"
        private const val KEY_DOZE_END_TIME = "doze_end_time"

        private const val KEY_SCHEDULE = "aod_schedule_time_enabled"
        private const val KEY_CHARGING = "aod_on_charge_enabled"
        private const val KEY_USER_ENABLED = "always_on_enabled_by_user"
        private const val KEY_TIME = "aod_schedule_time"
        private const val DEFAULT_TIME = "12:00-24:00"
    }
}
