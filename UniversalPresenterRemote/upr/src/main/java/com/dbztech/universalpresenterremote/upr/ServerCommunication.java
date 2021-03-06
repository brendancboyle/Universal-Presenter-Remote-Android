package com.dbztech.universalpresenterremote.upr;

import android.os.Handler;
import android.os.Looper;

import com.loopj.android.http.*;

import org.apache.http.Header;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Created by Brendan on 4/6/2014.
 */

public class ServerCommunication {

    private static String serverAddress = "https://universalpresenterremote.com/";
    public static int tempToken = 0;
    public static int token = 0;
    public static int uid = 0;
    public static int controlMode = 0;
    public static boolean serverAvailable = false;
    public static String gcmtoken = "";

    private static AsyncHttpClient clientInstance;

    public static AsyncHttpClient getClient() {
        if (Looper.myLooper() == null) {
            try {
                Looper.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (clientInstance == null) {
            try {
                /// We initialize a default Keystore
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                // We load the KeyStore
                trustStore.load(null, null);
                // We initialize a new SSLSocketFacrory
                MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
                // We set that all host names are allowed in the socket factory
                socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                // We initialize the Async Client
                clientInstance = new AsyncHttpClient();
                // We set the timeout to 30 seconds
                clientInstance.setTimeout(5 * 1000);
                // We set the SSL Factory
                clientInstance.setSSLSocketFactory(socketFactory);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }

        return clientInstance;
    }

    public static void newToken() {

        try {
            AsyncHttpClient client = getClient();
            // We initialize a GET http request
            System.out.println("UPR Cloud Query: NewToken");
            client.get(serverAddress + "NewSession", new TextHttpResponseHandler() {

                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                    System.err.println("UPR Error! NewSession failed.");
                }

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                    newTokenCallback(responseString);
                }
            });
        } catch (Exception e) {
            System.err.println("UPR Error! Callback: NewToken - " + e);
        }
    }

    public static void setupUid() {
        if (uid == 0) {
            uid = 9999 + (int) (Math.random() * 999999999);
        }
        System.out.println("UPR Set up: " + uid);
    }

    public static void newTokenCallback(String response) {
        tempToken = Integer.parseInt(response);
        System.out.println("UPR Token Assigned: " + tempToken);
        checkStatus();
    }

    public static void checkStatus() {
        try {
            AsyncHttpClient client = getClient();
            // We initialize a GET http request
            System.out.println("UPR Cloud Query: CheckToken");
            client.get(serverAddress + "TempSession?token=" + tempToken + "&holdfor=" + uid + "&gcmtoken=" + gcmtoken, new TextHttpResponseHandler() {

                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                    System.err.println("UPR Error! CheckToken failed.");
                }

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                    serverAvailable = true;
                    checkStatusCallback(responseString);
                }
            });
        } catch (Exception e) {
            System.err.println("UPR Error! Callback: CheckStatus - " + e);
        }

    }

    public static void reset() {
        token = 0;
        tempToken = 0;
    }

    public static void slideControl(int action) {
        try {
            AsyncHttpClient client = getClient();
            // We initialize a GET http request
            System.out.println("UPR Cloud Query: ChangeSlide");
            switch (action) {
                case 0:
                    client.get(serverAddress + "SlideDown?token=" + tempToken + "&holdfor=" + uid, new AsyncHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {

                        }

                        @Override
                        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    });
                    break;
                case 1:
                    client.get(serverAddress + "SlideUp?token=" + tempToken + "&holdfor=" + uid, new AsyncHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {

                        }

                        @Override
                        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    });
                    break;
                case 2:
                    client.get(serverAddress + "PlayMedia?token=" + tempToken + "&holdfor=" + uid, new AsyncHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {

                        }

                        @Override
                        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    });
                    break;
            }
        } catch (Exception e) {
            System.err.println("UPR Error! Callback: ChangeSlide - " + e);
        }
    }

    public static void checkStatusCallback(String response) {
        int incoming = Integer.parseInt(response);
        controlMode = incoming;
        switch (incoming) {
            case 0:
                //Token invalid or session taken
                newToken();
                break;
            case 1:
                //Waiting
                token = tempToken;
                break;
            case 2:
                //Ready to control
                break;
        }
    }
}
