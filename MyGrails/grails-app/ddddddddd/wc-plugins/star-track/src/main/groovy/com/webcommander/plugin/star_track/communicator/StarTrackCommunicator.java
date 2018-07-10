package com.webcommander.plugin.star_track.communicator;

import com.webcommander.plugin.star_track.util.StarTrackLogger;
import com.webcommander.util.HttpUtil;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by sajedur on 5/28/2015.
 */
public class StarTrackCommunicator {
    public static final String CONTENT_TYPE_XML = "text/xml; charset=utf-8";

    public static Map callUrl(String requestURL, String method, String requestData, String contentType, Map headers) throws Exception {
        StarTrackLogger.requestXml(requestData);
        HttpsURLConnection connection = null;
        String httpResponse = "", responseMessage = "";
        Integer httpCode = 0;
        String contentLength = "" + requestData.length(), accept = "text/xml";
        System.setProperty("https.protocols", "TLSv1,SSLv3,SSLv2Hello");
        connection = (HttpsURLConnection) new URL(requestURL).openConnection();
        setAcceptAllVerifier(connection);
        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        if (method.equalsIgnoreCase("post") || method.equalsIgnoreCase("put")) {
            connection.setDoInput(true);
            addHeaderToRequest(connection, headers);
            connection.setRequestProperty("Accept", accept);
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("Content_length", contentLength);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(requestData);
            writer.flush();
            writer.close();
        } else {
            connection.setRequestProperty("Pragma", "no-cache");
            addHeaderToRequest(connection, headers);
        }
        if(connection != null) {
            httpCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
            if(httpCode == 200 && responseMessage.equalsIgnoreCase("OK")) {
                httpResponse = HttpUtil.getResponseText(connection);
                StarTrackLogger.responseXml(httpResponse);
            } else {
                httpResponse = HttpUtil.getResponseText(connection);
                StarTrackLogger.responseXml(httpResponse);
                throw new Exception(httpResponse);
            }
        }
        Map result = new HashMap();
        result.put("code", httpCode);
        result.put("response", httpResponse);
        result.put("message", responseMessage);
        return result;
    }

    protected static void setAcceptAllVerifier(HttpsURLConnection connection) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSLv3");
        sc.init(null, ALL_TRUSTING_TRUST_MANAGER, new java.security.SecureRandom());
        SSLSocketFactory sslSocketFactory = sc.getSocketFactory();
        connection.setSSLSocketFactory(sslSocketFactory);
        connection.setHostnameVerifier(ALL_TRUSTING_HOSTNAME_VERIFIER);
    }

    private static final TrustManager[] ALL_TRUSTING_TRUST_MANAGER = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
    };

    private static final HostnameVerifier ALL_TRUSTING_HOSTNAME_VERIFIER  = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private static void addHeaderToRequest(HttpsURLConnection connection, Map headers) {
        Iterator iterator = headers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            connection.setRequestProperty(entry.getKey().toString(), entry.getValue().toString());
            iterator.remove();
        }
    }
}
