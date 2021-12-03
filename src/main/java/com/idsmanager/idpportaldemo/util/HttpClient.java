package com.idsmanager.idpportaldemo.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 2018/12/20
 *
 * @author Guilty_Crown
 */
public class HttpClient {

    private final static Charset CHARSET = Charset.forName("utf8");

    private final static HttpsTrustManager TRUST_MANAGER = new HttpsTrustManager();

    private final static HostnameVerifier DO_NOT_VERIFY = (hostname, session) -> true;

    private final static Logger LOG = LoggerFactory.getLogger(HttpClient.class);

    static {
        enableTrustInHttps();
    }

    private String url;

    private Map<String, String> headers = new HashMap();

    private Map<String, String> params = new HashMap();

    private Map<String, List<String>> responseHeaders = new HashMap<>();

    //跟随重定向，默认为true
    private boolean followRedirect = true;

    private String contentType;

    private byte[] requestBody;

    private HttpMethod method;

    public HttpClient(String url) {
        this.url = url;
    }

    public String execute() throws IOException {
        URL thisUrl = new URL(url);
        HttpURLConnection con;
        if (thisUrl.getProtocol().toLowerCase().equals("https")) {
            HttpsURLConnection https = (HttpsURLConnection) thisUrl.openConnection();
            con = https;
        } else {
            con = (HttpURLConnection) thisUrl.openConnection();
        }
        //请求头
        setHeaders(con);
        setRequestMethod(con);

        //打开输入模式和输出模式
        setMode(con);
        setRequestBody(con);

        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            readResponseHeader(con);
            InputStream err = con.getErrorStream();
            System.err.println("Server Response Code: " + responseCode);
            if (null != err) {
                String errAsString = new String(read(err), CHARSET);
                System.err.println(errAsString);
                return errAsString;
            }
            printHeaders();
            if (followRedirect()) {
                return resolvedRedirect();
            } else {
                System.err.println("Won`t Follow Redirect url : " + getResponseHeaders("Location") + ", Return Null");
                return null;
            }
        }
        int length = con.getContentLength();
        if (length < 0) {
            System.err.println("Server Response Empty Content");
        }
        InputStream is = con.getInputStream();
        return new String(read(is), CHARSET);
    }


    private String resolvedRedirect() {
        String location = getResponseHeaders("Location");
        if (null != location) {
            try {
                HttpClient followClient = followRedirectClient(location);
                return followClient.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private HttpClient followRedirectClient(String location) {
        System.err.println("Follow Redirect Url: " + location);
        return new HttpClient(location).get().followRedirect(true);
    }

    private void printHeaders() {
        if (!responseHeaders.isEmpty()) {
            System.err.println(">>>>>>>Response Header Begin");
            for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
                System.err.println(entry.getKey() +
                        ": " + entry.getValue());
            }
            System.err.println(">>>>>>>Response Header End");
        }
    }

    private void readResponseHeader(HttpURLConnection con) {
        this.responseHeaders = con.getHeaderFields();
    }

    //默认以;隔开
    private String getResponseHeaders(String key) {
        List<String> headerValues = responseHeaders.get(key);
        StringBuilder sb = new StringBuilder();
        int index = 0, size = headerValues.size();
        if (null != headerValues && !headerValues.isEmpty()) {
            for (String headerValue : headerValues) {
                sb.append(headerValue);
                index++;
                sb.append(index == size ? "" : ";");
            }
            return sb.toString();
        }
        return null;
    }

    private void setMode(HttpURLConnection con) {
        con.setDoInput(true);
        if (hasRequestBody()) {
            con.setDoOutput(true);
        }
    }

    protected byte[] read(InputStream is) throws IOException {
        ByteArrayOutputStream ous = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int length;
        while ((length = is.read(buffer)) != -1) {
            ous.write(buffer, 0, length);
        }
        ous.close();
        is.close();
        return ous.toByteArray();
    }

    private void setRequestMethod(HttpURLConnection con) {
        try {
            if (null != method) {
                con.setRequestMethod(method.name());
            }
        } catch (ProtocolException e) {
            LOG.warn("Set request method: " + method + "error", e);
        }
    }

    private void setRequestBody(HttpURLConnection con) throws IOException {
        if (hasRequestBody()) {
            try (OutputStream os = con.getOutputStream()) {
                if (hasRequestBody()) {
                    os.write(requestBody);
                }
            }
        }

    }

    public HttpClient followRedirect(boolean followRedirect) {
        this.followRedirect = followRedirect;
        return this;
    }

    public boolean followRedirect() {
        return followRedirect;
    }

    private boolean hasRequestBody() {
        return null != requestBody && requestBody.length > 0;
    }

    public HttpClient requestBody(byte[] requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    public HttpClient requestBody(String requestBody) {
        return requestBody(requestBody.getBytes(CHARSET));
    }

    public HttpClient addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public HttpClient contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public HttpMethod method() {
        return method;
    }

    public HttpClient method(HttpMethod method) {
        this.method = method;
        return this;
    }

    public HttpClient get() {
        return method(HttpMethod.GET);
    }

    public HttpClient head() {
        return method(HttpMethod.HEAD);
    }

    public HttpClient post() {
        return method(HttpMethod.POST);
    }

    public HttpClient put() {
        return method(HttpMethod.PUT);
    }

    public HttpClient delete() {
        return method(HttpMethod.DELETE);
    }

    public HttpClient patch() {
        return method(HttpMethod.PATCH);
    }

    public HttpClient options() {
        return method(HttpMethod.OPTIONS);
    }

    public HttpClient trace() {
        return method(HttpMethod.TRACE);
    }

    private void setHeaders(HttpURLConnection con) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }
        if (null != contentType) {
            con.setRequestProperty("Content-Type", contentType);
        }
    }

    private static void enableTrustInHttps() {
        // Create a trust manager that does not validate certificate chains
        HttpsTrustManager[] trustManagers = new HttpsTrustManager[]{TRUST_MANAGER};
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustManagers, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(DO_NOT_VERIFY);
        } catch (Exception e) {
            LOG.warn("create SSL SSLContext error", e);
        }
    }

    private static class HttpsTrustManager implements X509TrustManager {
        private HttpsTrustManager() {
        }

        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    enum HttpMethod {
        GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
    }

}
