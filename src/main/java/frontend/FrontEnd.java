package frontend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import httpclient.HttpClient;
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
            .addHandler("/frontend", new Handler(new InetSocketAddress(backendServerPort)))
            .build();
  }

  @Override
  public void close() {
    httpServer.close();
  }

  private static final class Handler implements HttpHandler {
    private final HttpClient httpClient;

    private Handler(InetSocketAddress address) {
      this.httpClient = new HttpClient(address);
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
      String response = "hello from java\n this is the end of the journey... for today";
      he.sendResponseHeaders(200, response.length());
      try (OutputStream os = he.getResponseBody()) {
        os.write(response.getBytes(Charset.defaultCharset()));
      }
      System.out.println("Served Client: " + he.getRemoteAddress());
    }
  }
}
