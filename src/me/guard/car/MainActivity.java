package me.guard.car;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.math.BigInteger;
import java.security.SecureRandom;

import static me.guard.car.Preferences.API_KEY_NAME;
import static me.guard.car.Preferences.SECRET_NAME;

public class MainActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Preferences preferences = new Preferences(this);
    if (preferences.has(API_KEY_NAME)) {
      showMainView(preferences);
    } else {
      showRegistrationView(this, preferences);
    }
  }

  private void showRegistrationView(final Context context, final Preferences preferences) {
    setContentView(R.layout.register);

    findViewById(R.id.buttonRegister).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

        String email = ((EditText) findViewById(R.id.emailText)).getText().toString().trim().toLowerCase();
        String phone = ((EditText) findViewById(R.id.phoneText)).getText().toString().replaceAll("[\\s-]*", "");

        if (email.isEmpty() || !email.matches("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}")) {
          showAlert("E-mail is missing or invalid", "Valid e-mail is needed for sending out alerts");
        } else if (!phone.isEmpty() && !phone.matches("\\+\\d{3,4}\\d{7,8}")) {
          showAlert("Phone number is invalid", "Enter a valid phone number with country code in the format +XXXXXXXXXXX");
        } else {
          try {
            preferences.set(API_KEY_NAME, new RegisterTask().execute(email, phone).get());
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
          showMainView(preferences);
        }
      }

      private void showAlert(String title, String message) {
        new AlertDialog.Builder(context)
          .setTitle(title)
          .setMessage(message)
          .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.cancel();
            }
          })
          .setIcon(android.R.drawable.ic_dialog_alert)
          .show();
      }
    });
  }

  private void showMainView(Preferences preferences) {
    setContentView(R.layout.main);
    fillApiKeyAndSecretFields(preferences);

    new TaskRunner().schedule(this);
  }

  private void fillApiKeyAndSecretFields(Preferences preferences) {
    ((EditText) findViewById(R.id.apiKeyText)).setText(preferences.get(API_KEY_NAME));
    ((EditText) findViewById(R.id.secretText)).setText(preferences.get(SECRET_NAME, new BigInteger(64, new SecureRandom()).toString(32)));
  }
}
