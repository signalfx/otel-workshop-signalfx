package httpclient;

import io.grpc.Context;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Tracer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.Charset;

public final class HttpClient {
  private static Tracer tracer = OpenTelemetry.getTracerProvider().get("httpclient.HttpClient");
  // Inject the span context into the request
  private static HttpTextFormat.Setter<HttpURLConnection> setter =
      (carrier, key, value) -> {
        if (carrier == null) {
          return;
        }
        carrier.setRequestProperty(key, value);
      };

  private final int port;

  public HttpClient(int port) {
    this.port = port;
  }

  public HttpResult sendGet(String path) {
    StringBuilder httpResponseContent = new StringBuilder();
    int httpResponseCode = 0;
    Span span = tracer.spanBuilder(path).setSpanKind(Span.Kind.CLIENT).startSpan();
    try (Scope ignored = tracer.withSpan(span)) {
      // Connect to the server locally
      URL url = new URL("http://127.0.0.1:" + port + "/" + path);
      HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

      span.setAttribute("component", "http");
      span.setAttribute("http.method", "GET");
      /*
       Only one of the following is required:
         - http.url
         - http.scheme, http.host, http.target
         - http.scheme, peer.hostname, peer.port, http.target
         - http.scheme, peer.ip, peer.port, http.target
      */
      span.setAttribute("http.url", url.toString());

      // Inject the request with the current Context/Span.
      OpenTelemetry.getPropagators()
          .getHttpTextFormat()
          .inject(Context.current(), httpURLConnection, setter);

      // Process the request
      httpURLConnection.setRequestMethod("GET");
      httpResponseCode = httpURLConnection.getResponseCode();
      BufferedReader in =
          new BufferedReader(
              new InputStreamReader(httpURLConnection.getInputStream(), Charset.defaultCharset()));
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        httpResponseContent.append(inputLine);
      }
      in.close();
      // Close the Span
      span.setStatus(Status.OK);
    } catch (IOException e) {
      // TODO create mapping for Http Error Codes <-> io.opentelemetry.trace.Status
      span.setStatus(Status.UNKNOWN.withDescription("HTTP Code: " + httpResponseCode));
    } finally {
      span.end();
    }

    return new HttpResult(httpResponseCode, httpResponseContent.toString());
  }
}
