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
package com.android.settings.applock

import android.os.Bundle
import com.android.settings.R
import com.prism.settings.preferences.BaseAppListFragment

class AppLockFragment : BaseAppListFragment() {

    override fun getSettingsKey(): String = "nt_locked_apps"

    override fun getCategoryTitle(): String = ""

    override fun getExcludedPackages(): List<String> {
        return requireContext().resources.getStringArray(R.array.nt_app_lock_exclude_list).toList()
    }

    override fun getInitialPreferencesXmlResId(): Int = R.xml.app_lock
    
    override fun requiresSecureAuthentication(): Boolean = true
    
    override fun getFragmentTitle(): String = getString(R.string.nt_app_lock)
    
    override fun getSelectedCategoryTitle(): String = getString(R.string.nt_app_locked_category_title)
    override fun getUnselectedCategoryTitle(): String = getString(R.string.nt_app_unlocked_category_title)
}
