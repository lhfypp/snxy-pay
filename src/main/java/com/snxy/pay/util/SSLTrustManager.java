package com.snxy.pay.util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.net.HttpURLConnection;
import java.net.URL;

public class SSLTrustManager implements javax.net.ssl.TrustManager,
            javax.net.ssl.X509TrustManager ,HostnameVerifier{
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
  
        public boolean isServerTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }
  
        public boolean isClientTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }
  
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
  
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
         
            @Override
        public boolean verify(String urlHostName, SSLSession session) { //允许所有主机
            return true;
        }

   //封装
public static HttpURLConnection connect(String strUrl) throws Exception {
         
         javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
         javax.net.ssl.TrustManager tm = new SSLTrustManager();
         trustAllCerts[0] = tm;
         javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
         sc.init(null, trustAllCerts, null);
         HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
          
         HttpsURLConnection.setDefaultHostnameVerifier((HostnameVerifier) tm);
          
        URL url = new URL(strUrl);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
      
        return urlConn;
    }


        
 }