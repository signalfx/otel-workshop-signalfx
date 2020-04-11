package httpserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.grpc.Context;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracingContextUtils;
import java.io.IOException;
import java.net.InetSocketAddress;

public final class HttpServer implements AutoCloseable {
  private static Tracer tracer = OpenTelemetry.getTracerProvider().get("httpserver.HttpServer");
  // Extract the context from http headers
  private static HttpTextFormat.Getter<HttpExchange> getter =
      (carrier, key) -> {
        Headers headers = carrier.getRequestHeaders();
        if (headers != null && headers.containsKey(key)) {
          return headers.get(key).get(0);
        }
        return null;
      };

  private final com.sun.net.httpserver.HttpServer httpServer;

  private HttpServer(com.sun.net.httpserver.HttpServer httpServer) {
    this.httpServer = httpServer;
    httpServer.start();
    System.out.println("Server ready on port: " + httpServer.getAddress().getPort());
  }

  public static Builder newBuilder(int serverPort) throws IOException {
    return new Builder(serverPort);
  }

  @Override
  public void close() {
    httpServer.stop(0);
  }

  public static final class Builder {
    private final com.sun.net.httpserver.HttpServer server;

    private Builder(int serverPort) throws IOException {
      server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(serverPort), 0);
    }

    public Builder addHandler(String path, HttpHandler handler) {
      server.createContext(path, new HttpHandlerWrapper(server, handler));
      return this;
    }

    public HttpServer build() {
      return new HttpServer(server);
    }
  }

  private static final class HttpHandlerWrapper implements HttpHandler {
    private final com.sun.net.httpserver.HttpServer server;
    private final HttpHandler wrappedHandler;

    private HttpHandlerWrapper(com.sun.net.httpserver.HttpServer server, HttpHandler wrappedHandler) {
      this.server = server;
      this.wrappedHandler = wrappedHandler;
    }

    @Override
    public void handle(HttpExchange httpExchange) {
      Context context =
          OpenTelemetry.getPropagators().getHttpTextFormat().extract(Context.current(),
          httpExchange, getter);
      Span span =
          tracer
              .spanBuilder(httpExchange.getHttpContext().getPath())
              .setParent(TracingContextUtils.getSpan(context))
              .setSpanKind(Span.Kind.SERVER)
              .startSpan();

      // Set the Semantic Convention
      span.setAttribute("http.method", "GET");
      span.setAttribute("http.scheme", "http");
      span.setAttribute("http.host", "localhost:" + server.getAddress().getPort());
      span.setAttribute("http.target", httpExchange.getHttpContext().getPath());

      try (Scope ignored = TracingContextUtils.currentContextWith(span)) {
        span.addEvent("Start Processing");

        wrappedHandler.handle(httpExchange);

        span.addEvent("Finish Processing");

        if (httpExchange.getResponseCode() != 200) {
          span.setStatus(Status.UNKNOWN.withDescription("HTTP Code: " + httpExchange.getResponseCode()));
        }
      } catch (IOException e) {
        span.setStatus(Status.UNKNOWN.withDescription("HTTP Code: " + httpExchange.getResponseCode()));
      } finally {
        // Close the span
        span.end();
      }
    }
  }
}
