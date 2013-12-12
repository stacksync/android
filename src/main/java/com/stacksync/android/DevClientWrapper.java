package com.stacksync.android;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;

import com.stacksync.android.utils.MySSLSocketFactory;

public class DevClientWrapper {

	public static HttpClient wrapClient(HttpClient base) {
		try {
            
            X509TrustManager tm = new X509TrustManager() {
 
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }
 
                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }
 
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            X509HostnameVerifier verifier = new X509HostnameVerifier() {
 
                                public boolean verify(String arg0, SSLSession arg1) {
                                        return true;
                                }
 
                                public void verify(String arg0, SSLSocket arg1) throws IOException {
                                       
                                }
 
                                public void verify(String arg0, X509Certificate arg1) throws SSLException {
                                       
                                }
 
                                public void verify(String arg0, String[] arg1, String[] arg2) throws SSLException {                                    
                                       
                                }
 
                /*
                @Override
                public void verify(String string, SSLSocket ssls) throws IOException {
                }
 
                @Override
                public void verify(String string, X509Certificate xc) throws SSLException {
                }
 
                @Override
                public void verify(String string, String[] strings, String[] strings1) throws SSLException {
                }
 
                @Override
                public boolean verify(String string, SSLSession ssls) {
                    return true;
                }
                */
                               
               
            };
            
            SSLContext ctx = SSLContext.getInstance("TLS");
            //SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(null, new TrustManager[]{tm}, null);
            
            HttpsURLConnection
			.setDefaultSSLSocketFactory(ctx.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(verifier);
            
            SSLSocketFactory ssf = new MySSLSocketFactory(ctx);
            
            ssf.setHostnameVerifier(verifier);
            ClientConnectionManager ccm = base.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, 443));
            return new DefaultHttpClient(ccm, base.getParams());
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
	}
}
