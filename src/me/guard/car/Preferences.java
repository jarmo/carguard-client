package me.guard.car;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class Preferences {
  public static final String PREFERENCES_NAME = "CarGuard";
  public static final String API_KEY_NAME = "apiKey";
  public static final String SECRET_NAME = "secret";

  private Context context;

  public Preferences(Context context) {
    this.context = context;
  }

  public String get(String name, String defaultValue) {
    SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);

    if (sharedPreferences.contains(name)) {
      return sharedPreferences.getString(name, null);
    } else {
      SharedPreferences.Editor editablePreferences = sharedPreferences.edit();
      editablePreferences.putString(name, defaultValue);
      editablePreferences.commit();
      return defaultValue;
    }
  }

  public String get(String name) {
    return get(name, "");
  }
}
