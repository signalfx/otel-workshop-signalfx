package httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

public final class HttpClient {
  private final InetSocketAddress address;

  public HttpClient(InetSocketAddress address) {
    this.address = address;
  }

  public void sendGet(String path) throws IOException {
    String url = address.toString() + "/" + path;

    HttpURLConnection httpClient =
        (HttpURLConnection) new URL(url).openConnection();

    // Optional default is GET
    httpClient.setRequestMethod("GET");

    // Add request header
    httpClient.setRequestProperty("User-Agent", "Mozilla/5.0");

    int responseCode = httpClient.getResponseCode();
    System.out.println("\nSending 'GET' request to URL : " + url);
    System.out.println("Response Code : " + responseCode);

    try (BufferedReader in = new BufferedReader(
        new InputStreamReader(httpClient.getInputStream()))) {

      StringBuilder response = new StringBuilder();
      String line;

      while ((line = in.readLine()) != null) {
        response.append(line);
      }

      //print result
      System.out.println(response.toString());
    }

  }
}
