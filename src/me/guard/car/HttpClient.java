package me.guard.car;

import android.util.Base64;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.util.Base64.DEFAULT;

public class HttpClient {

  public void post(String data) {
    try {
      HttpURLConnection conn = openConnection("http://leetor.no-ip.org:8010/");
      conn.setRequestProperty("Authorization", "Basic " + Base64.encodeToString("car:guard".getBytes(), DEFAULT));
      conn.setDoOutput(true);
      OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
      wr.write(data);
      wr.flush();
      wr.close();

      if (conn.getResponseCode() != 200) {
        throw new RuntimeException("Failed to post data, http status: " + conn.getResponseCode());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private HttpURLConnection openConnection(String url) throws IOException {
    return (HttpURLConnection) (new URL(url).openConnection());
  }
}
