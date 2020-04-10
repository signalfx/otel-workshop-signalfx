package backend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public final class BackEnd implements AutoCloseable {
  private final HttpServer httpServer;

  public BackEnd() throws IOException {
    Dotenv dotenv = Dotenv.load();
    int backendServerPort = Integer.parseInt(dotenv.get("BACKEND_SERVER_PORT"));
    this.httpServer =
        HttpServer.newBuilder(new InetSocketAddress(backendServerPort))
            .addHandler("/backend", new Handler())
            .build();
  }

  @Override
  public void close() {
    httpServer.close();
  }

  private static final class Handler implements HttpHandler {
    @Override
    public void handle(HttpExchange he) throws IOException {

      // Process the request
      String response = "hello from java\n this is the end of the journey... for today";
      he.sendResponseHeaders(200, response.length());
      try (OutputStream os = he.getResponseBody()) {
        os.write(response.getBytes(Charset.defaultCharset()));
      }
      System.out.println("Served Client: " + he.getRemoteAddress());
    }
  }
}
