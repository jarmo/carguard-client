package me.guard.car;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient {

  private String path;

  public HttpClient(String path) {
    this.path = path;
  }

  public String post(String data) {
    try {
      HttpURLConnection conn = openConnection("https://carguard.me" + path);
      conn.setDoOutput(true);
      OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
      wr.write(data);
      wr.flush();
      wr.close();

      if (conn.getResponseCode() != 200 && conn.getResponseCode() != 404) {
        throw new RuntimeException("Failed to post data, http status: " + conn.getResponseCode());
      }

      String responseBody = "";
      BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        responseBody += line;
      }

      return responseBody;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private HttpURLConnection openConnection(String url) throws IOException {
    return (HttpURLConnection) (new URL(url).openConnection());
  }
}
