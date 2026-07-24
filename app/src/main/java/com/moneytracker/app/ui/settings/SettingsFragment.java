package com.moneytracker.app.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.moneytracker.app.R;
import com.moneytracker.app.databinding.FragmentSettingsBinding;
import com.moneytracker.app.ui.widget.SegmentedToggle;
import com.moneytracker.app.util.LocaleHelper;
import com.moneytracker.app.util.OnboardingPrefs;
import com.moneytracker.app.util.ThemePrefs;
import com.moneytracker.app.util.ViewAnimations;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private SegmentedToggle languageToggle;
    private SegmentedToggle themeToggle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        ViewAnimations.fadeInContent(view);

        String userName = OnboardingPrefs.getUserName(requireContext());
        if (!userName.isEmpty()) {
            binding.cardProfile.setVisibility(View.VISIBLE);
            binding.textProfileName.setText(userName);
        }

        int selectedColor = requireContext().getColor(R.color.on_accent_text);
        int unselectedColor = requireContext().getColor(R.color.text_secondary);

        languageToggle = new SegmentedToggle(binding.languageContainer, binding.languageIndicator,
                new TextView[]{binding.optionEnglish, binding.optionCzech, binding.optionGerman},
                selectedColor, unselectedColor);
        languageToggle.setOnSegmentSelectedListener(index -> changeLanguage(languageForIndex(index)));

        themeToggle = new SegmentedToggle(binding.themeContainer, binding.themeIndicator,
                new TextView[]{binding.optionThemeLight, binding.optionThemeDark, binding.optionThemeSystem},
                selectedColor, unselectedColor);
        themeToggle.setOnSegmentSelectedListener(index -> viewModel.setTheme(themeForIndex(index)));

        binding.switchHideBalance.setOnClickListener(v ->
                viewModel.setHideBalance(binding.switchHideBalance.isChecked()));

        viewModel.getLanguage().observe(getViewLifecycleOwner(), language ->
                languageToggle.setSelectedIndex(indexForLanguage(language)));
        viewModel.getTheme().observe(getViewLifecycleOwner(), theme ->
                themeToggle.setSelectedIndex(indexForTheme(theme)));
        viewModel.getHideBalance().observe(getViewLifecycleOwner(), hidden ->
                binding.switchHideBalance.setChecked(Boolean.TRUE.equals(hidden)));
    }

    private void changeLanguage(String language) {
        if (viewModel.setLanguage(language)) {
            requireActivity().recreate();
        }
    }

    private static String languageForIndex(int index) {
        switch (index) {
            case 1:
                return LocaleHelper.LANGUAGE_CZECH;
            case 2:
                return LocaleHelper.LANGUAGE_GERMAN;
            default:
                return LocaleHelper.LANGUAGE_ENGLISH;
        }
    }

    private static int indexForLanguage(String language) {
        if (LocaleHelper.LANGUAGE_CZECH.equals(language)) {
            return 1;
        }
        if (LocaleHelper.LANGUAGE_GERMAN.equals(language)) {
            return 2;
        }
        return 0;
    }

    private static String themeForIndex(int index) {
        switch (index) {
            case 1:
                return ThemePrefs.THEME_DARK;
            case 2:
                return ThemePrefs.THEME_SYSTEM;
            default:
                return ThemePrefs.THEME_LIGHT;
        }
    }

    private static int indexForTheme(String theme) {
        if (ThemePrefs.THEME_DARK.equals(theme)) {
            return 1;
        }
        if (ThemePrefs.THEME_SYSTEM.equals(theme)) {
            return 2;
        }
        return 0;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
