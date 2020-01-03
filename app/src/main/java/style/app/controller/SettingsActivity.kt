package style.app.controller

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import style.app.R

class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences)
        }
    }

    companion object {
        fun bindSummaryValue(preference: Preference) {
//            preference.onPreferenceChangeListener = changeListener
//            changeListener.onPreferenceChange(
//                preference,
//                PreferenceManager
//                    .getDefaultSharedPreferences(preference.context)
//                    .getString(preference.key, "")
//            )
        }

        private val changeListener = object: Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                val value = newValue.toString()
                if (preference is EditTextPreference) {
                    preference.setOnBindEditTextListener {
                        it.inputType = InputType.TYPE_CLASS_NUMBER
                    }
                    preference.setSummary(value)
                }
                return false
            }
        }
    }


}
