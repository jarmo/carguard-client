package me.guard.car;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class Preferences {
  public static final String PREFERENCES_NAME = "CarGuard";
  public static final String API_KEY_NAME = "apiKey";
  public static final String SECRET_NAME = "secret";
  private final SharedPreferences sharedPreferences;

  public Preferences(Context context) {
    this.sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
  }

  public String get(String name, String defaultValue) {
    if (has(name)) {
      return sharedPreferences.getString(name, null);
    } else {
      set(name, defaultValue);
      return defaultValue;
    }
  }

  public void set(String name, String value) {
    SharedPreferences.Editor editablePreferences = sharedPreferences.edit();
    editablePreferences.putString(name, value);
    editablePreferences.commit();
  }

  public boolean has(String name) {
    return sharedPreferences.contains(name);
  }

  public String get(String name) {
    return get(name, null);
  }
}
