package httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracingContextUtils;
import java.io.IOException;
import java.net.InetSocketAddress;

public final class HttpServer implements AutoCloseable {
  private static Tracer tracer = OpenTelemetry.getTracerProvider().get("httpserver.HttpServer");
  private final com.sun.net.httpserver.HttpServer httpServer;

  private HttpServer(com.sun.net.httpserver.HttpServer httpServer) {
    this.httpServer = httpServer;
    httpServer.start();
    System.out.println("Server ready on: " + httpServer.getAddress().toString());
  }

  public static Builder newBuilder(InetSocketAddress address) throws IOException {
    return new Builder(address);
  }

  @Override
  public void close() {
    httpServer.stop(0);
  }

  public static final class Builder {
    private final com.sun.net.httpserver.HttpServer server;

    private Builder(InetSocketAddress address) throws IOException {
      server = com.sun.net.httpserver.HttpServer.create(address, 0);
    }

    public Builder addHandler(String path, HttpHandler handler) {
      server.createContext(path, new HttpHandlerWrapper(handler));
      return this;
    }

    public HttpServer build() {
      return new HttpServer(server);
    }
  }

  private static final class HttpHandlerWrapper implements HttpHandler {
    private final HttpHandler wrappedHandler;

    private HttpHandlerWrapper(HttpHandler wrappedHandler) {
      this.wrappedHandler = wrappedHandler;
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
      Span span =
          tracer
              .spanBuilder(he.getHttpContext().getPath())
              .setSpanKind(Span.Kind.SERVER)
              .startSpan();
      try {
        span.setAttribute("uri", he.getRequestURI().toString());

        try (Scope ignored = TracingContextUtils.currentContextWith(span)) {
          // Pass the request to the wrapped
          wrappedHandler.handle(he);
        }

        // Everything works fine in this example
        span.setStatus(Status.OK);
      } finally {
        // Close the span
        span.end();
      }
    }
  }
}
