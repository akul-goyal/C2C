package c2c.com.cartalk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Handles everything having to do with memory
 */

class MemoryManager {

    //---Variables----------------------------------------------------------------------------------

    final String nameKey = "nameKey";
    final String plateKey = "plateKey";
    final String typeKey = "typeKey";
    final String colorKey = "colorKey";


    //---SAVE---------------------------------------------------------------------------------------

    void saveProfile(Activity activity, String name, String plate, String type, String color) {

        PreferenceManager.getDefaultSharedPreferences(activity).edit().putString(nameKey, name.toLowerCase()).apply();
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putString(plateKey, plate.toLowerCase()).apply();
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putString(typeKey, type.toLowerCase()).apply();
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putString(colorKey, color.toLowerCase()).apply();
    }

    //---LOAD---------------------------------------------------------------------------------------

    String[] getProfileInfo(Activity activity) {
        String[] info = new String[4];

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        info[0] = sharedPreferences.getString(nameKey, "default name");
        info[1] = sharedPreferences.getString(plateKey, "default plate");
        info[2] = sharedPreferences.getString(typeKey, "default type");
        info[3] = sharedPreferences.getString(colorKey, "default color");

        return info;
    }
}
