package com.ferg.awfulapp.preferences.fragments;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import androidx.preference.Preference;
import androidx.annotation.NonNull;

import com.ferg.awfulapp.AwfulActivity;
import com.ferg.awfulapp.NavigationEvent;
import com.ferg.awfulapp.R;
import com.ferg.awfulapp.constants.Constants;
import com.ferg.awfulapp.dialog.Changelog;
import com.ferg.awfulapp.preferences.SettingsActivity;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by baka kaba on 04/05/2015.
 * <p>
 * The SettingsFragment that forms the root of the Settings hierarchy
 */
public class RootSettings extends SettingsFragment {

    {
        SETTINGS_XML_RES_ID = R.xml.rootsettings;

        SUBMENU_OPENING_KEYS = new int[]{
                R.string.pref_key_theme_menu_item,
                R.string.pref_key_forum_index_menu_item,
                R.string.pref_key_thread_menu_item,
                R.string.pref_key_posts_menu_item,
                R.string.pref_key_images_menu_item,
                R.string.pref_key_misc_menu_item,
                R.string.pref_key_account_menu_item
        };

        prefClickListeners.put(new AboutListener(), new int[]{
                R.string.pref_key_about_menu_item
        });
        prefClickListeners.put(new ThreadListener(), new int[]{
                R.string.pref_key_open_thread_menu_item
        });
        // TODO: fix
        prefClickListeners.put(new ChangelogListener(), new int[]{
                R.string.pref_key_changelog_menu_item
        });
        prefClickListeners.put(new ExportListener(), new int[]{
                R.string.pref_key_export_settings_menu_item
        });
        prefClickListeners.put(new ImportListener(), new int[]{
                R.string.pref_key_import_settings_menu_item
        });
    }

    @NonNull
    @Override
    public String getTitle() {
        return getString(R.string.settings_activity_title);
    }

    /**
     * Listener for the 'About...' option
     */
    private class AboutListener implements Preference.OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            CharSequence app_version = getText(R.string.app_name);
            try {
                app_version = app_version + " " +
                        getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0)
                                .versionName;
            } catch (PackageManager.NameNotFoundException e) {
                // rather unlikely, just show app_name without version
            }
            // Build the text for the About dialog
            Resources res = getResources();
            String aboutText = getString(R.string.about_contributors_title) + "\n\n";
            aboutText += StringUtils.join(res.getStringArray(R.array.about_contributors_array), '\n');
            aboutText += "\n\n" + getString(R.string.about_libraries_title) + "\n\n";
            aboutText += StringUtils.join(res.getStringArray(R.array.about_libraries_array), '\n');
            Dialog about = new AlertDialog.Builder(getActivity())
                    .setTitle(app_version)
                    .setMessage(aboutText)
                    .setNeutralButton(android.R.string.ok, (dialog, which) -> {
                    })
                    .show();

            AwfulActivity activity = (AwfulActivity) getActivity();
            activity.setPreferredFont(about.findViewById(androidx.appcompat.R.id.alertTitle));
            activity.setPreferredFont(about.findViewById(android.R.id.message));
            activity.setPreferredFont(about.findViewById(android.R.id.button3));

            return true;
        }
    }

    private class ChangelogListener implements Preference.OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            Changelog.showDialog(getActivity(), null);
            return true;
        }
    }


    /**
     * Listener for 'Go to the Awful thread' option
     */
    private class ThreadListener implements Preference.OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            navigate(new NavigationEvent.Thread(Constants.AWFUL_THREAD_ID, null, null));
            return true;
        }
    }

    /**
     * Listener for the 'Export settings' option
     */
    private class ExportListener implements Preference.OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Activity context = getActivity();
            PackageInfo pInfo;
            try {
                pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                // super unlikely
                e.printStackTrace();
                return false;
            }
            Calendar date = Calendar.getInstance();

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                    .setType("*/*")
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .putExtra(Intent.EXTRA_TITLE, String.format(Locale.US, "awful-%d-%d-%d-%d.settings",
                            pInfo.versionCode, date.get(Calendar.DATE), date.get(Calendar.MONTH) + 1, date.get(Calendar.YEAR)));

            getActivity().startActivityForResult(Intent.createChooser(intent, getString(R.string.export_settings_chooser_title)), SettingsActivity.SETTINGS_EXPORT);
            return true;
        }
    }

    /**
     * Listener for the 'Import settings' option
     */
    private class ImportListener implements Preference.OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            // ACTION_GET_CONTENT may return URIs for deleted content as well,
            // which is super confusing. workarounds seem like more trouble
            // than they're worth right now.
            // see https://stackoverflow.com/questions/55122556
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                    .setType("*/*")
                    .addCategory(Intent.CATEGORY_OPENABLE);
            getActivity().startActivityForResult(Intent.createChooser(intent, getString(R.string.import_settings_chooser_title)), SettingsActivity.SETTINGS_IMPORT);
            return true;
        }
    }
}