package me.guard.car;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterTask extends AsyncTask<String, Void, String> {
  @Override
  protected String doInBackground(final String... credentials) {
    Map<String, String> params = new HashMap<String, String>() {{
      put("email", credentials[0]);
      String phone = credentials[1];
      put("phone", phone.isEmpty() ? null : phone);
    }};
    return new HttpClient("/register").post(new JSONObject(params).toString());
  }
}
