package me.guard.car;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.UUID;

import static me.guard.car.Preferences.API_KEY_NAME;
import static me.guard.car.Preferences.SECRET_NAME;

public class MainActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    fillTextFields();

    new TaskRunner().schedule(this);
  }

  private void fillTextFields() {
    Preferences preferences = new Preferences(this);
    ((EditText)findViewById(R.id.apiKeyText)).setText(preferences.get(API_KEY_NAME, UUID.randomUUID().toString()));
    ((EditText) findViewById(R.id.secretText)).setText(preferences.get(SECRET_NAME, new BigInteger(64, new SecureRandom()).toString(32)));
  }
}
