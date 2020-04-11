package backend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public final class BackEnd implements AutoCloseable {
  private final HttpServer httpServer;

  public BackEnd() throws IOException {
    Dotenv dotenv = Dotenv.load();
    int backendServerPort = Integer.parseInt(dotenv.get("BACKEND_SERVER_PORT"));
    this.httpServer =
        HttpServer.newBuilder(backendServerPort)
            .addHandler("/backend", new Handler())
            .build();
  }

  @Override
  public void close() {
    httpServer.close();
  }

  private static final class Handler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
      // Process the request
      String response = "hello from java\n this is the end of the journey... for today";
      try {
        doWork();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      httpExchange.sendResponseHeaders(200, response.length());
      try (OutputStream os = httpExchange.getResponseBody()) {
        os.write(response.getBytes(Charset.defaultCharset()));
      }
    }

    private void doWork() throws InterruptedException {
      Thread.sleep(1000, 0);
    }
  }

  /**
   * Main method to run the example.
   *
   * @param args It is not required.
   * @throws IOException Something might go wrong.
   */
  public static void main(String[] args) throws IOException {
    final BackEnd backEnd = new BackEnd();

    // Gracefully close the servers
    Runtime.getRuntime().addShutdownHook(new Thread(backEnd::close));
  }
}
