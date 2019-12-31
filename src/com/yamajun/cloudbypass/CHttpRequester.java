package com.yamajun.cloudbypass;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class CHttpRequester {

  private static final String URL_TEMPLATE = "%s://%s/cdn-cgi/l/chk_jschl";
  private CFirewallDetector cFirewallDetector;
  private CChallengeResolve cChallengeResolve;
  private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";
  private static final int TIMEOUT = 10000;
  //private static Proxy PROXY = null;

  public CHttpRequester() {
    this.cFirewallDetector = new CFirewallDetector();
    this.cChallengeResolve = new CChallengeResolve(
            new CJavascriptChallengeHelper(new ScriptEngineManager().getEngineByName("nashorn")
            ));
  }

  public static boolean hostAvailabilityCheck(String host, int port) {
    try (Socket s = new Socket(host, port)) {
      return true;
    } catch (IOException ex) {}
    return false;
  }

  public Document get(String uri)
      throws URISyntaxException, IOException, InterruptedException, ScriptException {
    Document response = Jsoup.connect(uri)
        .timeout(TIMEOUT)
        .userAgent(USER_AGENT)
        .ignoreHttpErrors(true)
        .get();

    if (cFirewallDetector.isBehindFirewall(response)) {
      URL url = new URL(uri);
      Map<String, String> params = cChallengeResolve.getPathParams(response, url.getHost());
      String urlToConnect = String.format(URL_TEMPLATE, url.getProtocol(), url.getHost());
      Thread.sleep(5000);
      return Jsoup.connect(urlToConnect).data(params).timeout(TIMEOUT).userAgent(USER_AGENT)
          .get();
    } else {
      return response;
    }
  }

  public Connection.Response getFile(final String uri) throws URISyntaxException, IOException, InterruptedException, ScriptException {
    return this.getFile(uri, null);
  }

  public Connection.Response getFile(final String uri, final Map<String, String> headers) throws IOException, InterruptedException, ScriptException {
    final Connection conn = Jsoup.connect(uri)/*.proxy(PROXY)*/;
    if (headers != null && headers.size() > 0) {
      for (final Map.Entry<String, String> h : headers.entrySet()) {
        conn.header(h.getKey(), h.getValue());
      }
    }
    final Connection.Response resp = conn.timeout(10000).userAgent(USER_AGENT).ignoreContentType(true).ignoreHttpErrors(true).execute();
    Document document = null;
    if (resp.statusCode() == 200 && resp.contentType().startsWith("text/")) {
      document = resp.parse();
    }
    if (document != null && this.cFirewallDetector.isBehindFirewall(document)) {
      final URL url = new URL(uri);
      final Map<String, String> params = this.cChallengeResolve.getPathParams(document, url.getHost());
      final String urlToConnect = String.format("%s://%s/cdn-cgi/l/chk_jschl", url.getProtocol(), url.getHost());
      Thread.sleep(5000L);
      return Jsoup.connect(urlToConnect)
              //.proxy(PROXY)
              .data(params).timeout(10000).userAgent(USER_AGENT).ignoreContentType(true).execute();
    }
    return resp;
  }

  private Connection.Response buzi(final String uri, final Map<String, String> headers) throws IOException, InterruptedException, ScriptException {
    final Connection conn = Jsoup.connect(uri);
    if (headers != null && headers.size() > 0) {
      for (final Map.Entry<String, String> h : headers.entrySet()) {
        conn.header(h.getKey(), h.getValue());
      }
    }
    final Connection.Response resp = conn.timeout(10000).userAgent(USER_AGENT).ignoreContentType(true).ignoreHttpErrors(true).execute();
    Document document = null;
    if (resp.statusCode() == 200 && resp.contentType().startsWith("text/")) {
      document = resp.parse();
    }
    if (document != null && this.cFirewallDetector.isBehindFirewall(document)) {
      final URL url = new URL(uri);
      final Map<String, String> params = this.cChallengeResolve.getPathParams(document, url.getHost());
      final String urlToConnect = String.format("%s://%s/cdn-cgi/l/chk_jschl", url.getProtocol(), url.getHost());
      Thread.sleep(5000L);
      return Jsoup.connect(urlToConnect)
              .data(params).timeout(10000).userAgent(USER_AGENT).ignoreContentType(true).execute();
    }
    return resp;
  }

}
