package frontend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import httpclient.HttpClient;
import httpclient.HttpResult;
import httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class FrontEnd implements AutoCloseable {
  private final HttpServer httpServer;

  public FrontEnd() throws IOException {
    Dotenv dotenv = Dotenv.load();
    int frontendServerPort = Integer.parseInt(dotenv.get("FRONTEND_SERVER_PORT"));
    int backendServerPort = Integer.parseInt(dotenv.get("BACKEND_SERVER_PORT"));
    this.httpServer =
        HttpServer.newBuilder(new InetSocketAddress(frontendServerPort))
            .addHandler("/frontend", new Handler(backendServerPort))
            .build();
  }

  @Override
  public void close() {
    httpServer.close();
  }

  private static final class Handler implements HttpHandler {
    private final HttpClient httpClient;

    private Handler(int backendServerPort) {
      System.out.println("Backend address: 127.0.0.1" + backendServerPort + "/" + "backend");
      this.httpClient = new HttpClient(backendServerPort);
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
      HttpResult result = httpClient.sendGet("backend");
      he.sendResponseHeaders(
          result.getHttpResponseCode(), result.getHttpResponseContent().length());
      try (OutputStream os = he.getResponseBody()) {
        os.write(result.getHttpResponseContent().getBytes(Charset.defaultCharset()));
      }
    }
  }
}
