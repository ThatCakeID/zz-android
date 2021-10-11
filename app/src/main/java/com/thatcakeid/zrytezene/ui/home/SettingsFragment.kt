package com.thatcakeid.zrytezene.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceFragmentCompat
import com.thatcakeid.zrytezene.ExtraMetadata.setWatermarkColors
import com.thatcakeid.zrytezene.R
import com.thatcakeid.zrytezene.databinding.FragmentSettingsBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private val binding: FragmentSettingsBinding by viewBinding(FragmentSettingsBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setWatermarkColors(binding.textWatermark, binding.watermarkRoot)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        childFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}