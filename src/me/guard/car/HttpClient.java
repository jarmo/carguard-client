package me.guard.car;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient {

  private String apiKey;

  public HttpClient(String apiKey) {
    this.apiKey = apiKey;
  }

  public void post(String data) {
    try {
      HttpURLConnection conn = openConnection("http://leetor.no-ip.org:8010/map/" + this.apiKey);
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
