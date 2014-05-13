package me.guard.car;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.UUID;

public class MainActivity extends Activity {

  public static final String PREFERENCES_NAME = "CarGuard";
  public static final String API_KEY = "apiKey";
  private static final String SECRET = "secret";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    ((EditText)findViewById(R.id.apiKeyText)).setText(getApiKey());
    ((EditText)findViewById(R.id.secretText)).setText(getSecret());

    new TaskRunner().schedule(this);
  }

  private String getApiKey() {
    return getPreference(API_KEY, UUID.randomUUID().toString());
  }

  private String getSecret() {
    return getPreference(SECRET, new BigInteger(64, new SecureRandom()).toString(32));
  }

  private String getPreference(String name, String defaultValue) {
    SharedPreferences preferences = getPreferences();
    if (preferences.contains(name)) {
      return preferences.getString(name, null);
    } else {
      SharedPreferences.Editor editablePreferences = preferences.edit();
      editablePreferences.putString(name, defaultValue);
      editablePreferences.commit();
      return defaultValue;
    }
  }

  public SharedPreferences getPreferences() {
    return getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
  }
}
