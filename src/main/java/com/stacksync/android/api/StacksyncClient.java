package com.stacksync.android.api;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import com.stacksync.android.DevClientWrapper;
import com.stacksync.android.Utils;
import com.stacksync.android.exceptions.CancelledTaskException;
import com.stacksync.android.exceptions.FolderNotFoundException;
import com.stacksync.android.exceptions.NoInternetConnectionException;
import com.stacksync.android.exceptions.NotLoggedInException;
import com.stacksync.android.exceptions.UnauthorizedException;
import com.stacksync.android.exceptions.UnexpectedResponseException;
import com.stacksync.android.exceptions.UnexpectedStatusCodeException;
import com.stacksync.android.task.MyAsyncTask;
import com.stacksync.android.utils.Constants;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class StacksyncClient {

    private static String TAG = "StacksyncClient";

    private Context context;
    private HttpClient client;
    private boolean isLoggedin = false;

    private StacksyncConsumer consumer;
    private String authorizeUrl;
    private String apiUrl = "http://api.stacksync.com:8080";
    private String verifier;
    private OAuthProvider provider;


    public StacksyncClient(Context context, boolean developmentMode) {

        this.context = context;

        BasicHttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, Constants.TIMEOUT_CONNECTION);
        HttpConnectionParams.setSoTimeout(httpParameters, Constants.TIMEOUT_SOCKET);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
        schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));

        ClientConnectionManager cm = new ThreadSafeClientConnManager(httpParameters, schemeRegistry);

        client = new DefaultHttpClient(cm, httpParameters);

        if (developmentMode) {
            client = DevClientWrapper.wrapClient(client);
        }

        consumer = new StacksyncConsumer("b3af4e669daf880fb16563e6f36051b105188d413",
                "c168e65c18d75b35d8999b534a3776cf");
        provider = new DefaultOAuthProvider(
                "http://api.stacksync.com:8080/oauth/request_token",
                "http://api.stacksync.com:8080/oauth/access_token",
                "http://api.stacksync.com:8080/oauth/authorize");
        provider.setOAuth10a(true);
        provider.setListener(new StacksyncProviderListener());
    }


    public boolean isLoggedIn() {
        return isLoggedin;
    }

    public StacksyncConsumer getConsumer() {
        return consumer;
    }

    public void getRequestToken() throws NoInternetConnectionException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException {


        if (!Utils.isNetworkConnected(context)) {
            throw new NoInternetConnectionException();
        }


        authorizeUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
    }

    public void authorize(String email, String password) throws NoInternetConnectionException,
            IOException, URISyntaxException, OAuthNotAuthorizedException {


        if (!Utils.isNetworkConnected(context)) {
            throw new NoInternetConnectionException();
        }

        HttpPost method = new HttpPost(authorizeUrl);
        method.setHeader("Content-Type", "application/x-www-form-urlencoded");

        String postBody = String.format("email=%s&password=%s&permission=%s", URLEncoder.encode(email, "UTF-8"),  URLEncoder.encode(password, "UTF-8"), "allow");
        StringEntity entity = new StringEntity(postBody);
        method.setEntity(entity);

        HttpResponse response = client.execute(method);
        String aux_content_type = response.getEntity().getContentType().getValue();
       if ("application/x-www-form-urlencoded".equals(response.getEntity().getContentType().getValue())) {
           BufferedReader rd = new BufferedReader(
                   new InputStreamReader(response.getEntity().getContent()));

           String lineAux = "";
           String line = "";

           while ((lineAux = rd.readLine()) != null) {
               line += lineAux;
           }
           URI uri = new URI(line);
           List<NameValuePair> result;
           result = URLEncodedUtils.parse(uri, "UTF-8");

           for (NameValuePair a : result) {
               if (a.getName() == "verifier") {
                   verifier = a.getValue();
               }
           }
       }
       else{
          throw new OAuthNotAuthorizedException();
       }

    }

    public void getAccessToken() throws NoInternetConnectionException,
            OAuthCommunicationException, OAuthExpectationFailedException,
            OAuthNotAuthorizedException, OAuthMessageSignerException {


        if (!Utils.isNetworkConnected(context)) {
            throw new NoInternetConnectionException();
        }
        provider.retrieveAccessToken(consumer, verifier.trim());

    }

    public void logout() {
        isLoggedin = false;
    }

    public ListResponse listDirectory(String fileId, Boolean retry) throws NoInternetConnectionException,
            FolderNotFoundException, UnexpectedStatusCodeException, UnexpectedResponseException, NotLoggedInException,
            UnauthorizedException, IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {

        if (!isLoggedin) {
            throw new NotLoggedInException();
        }

        try {
            return listDirectory(fileId);
        } catch (UnauthorizedException e) {

            if (retry) {
                Log.w(TAG, "Retrying to login.");
                return listDirectory(fileId);

            } else {
                throw e;
            }
        }
    }

    public ListResponse listDirectory(String folderId) throws NoInternetConnectionException, FolderNotFoundException,
            UnexpectedStatusCodeException, UnexpectedResponseException, NotLoggedInException, UnauthorizedException,
            IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {

        if (!Utils.isNetworkConnected(context)) {
            throw new NoInternetConnectionException();
        }

        if (!isLoggedin) {
            throw new NotLoggedInException();
        }

        JSONObject jObject = null;

        String path = String.format("/folder/%s/contents", folderId.toString());


        String url = Utils.buildUrl("storageUrl", path, null);

        HttpGet method = new HttpGet(url);
        /*Pregunta Adri*/
        method.getParams().setIntParameter("http.socket.timeout", Constants.TIMEOUT_CONNECTION);
        method.setHeader(FilesConstants.STACKSYNC_API, "v2");
        consumer.sign(method);

        ListResponse result = new ListResponse();

        try {
            FilesResponse response = new FilesResponse(client.execute(method));
            int statusCode = response.getStatusCode();

            if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_ACCEPTED) {

                String body = response.getResponseBodyAsString();
                jObject = new JSONObject(body);

                result.setSucced(true);
                result.setStatusCode(statusCode);
                result.setFileId(folderId);
                result.setMetadata(jObject);

            } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                Log.e(TAG, "Folder not found");
                throw new FolderNotFoundException("Folder not found", statusCode);
            } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                isLoggedin = false;
                throw new UnauthorizedException("User unauthorized", statusCode);
            } else {
                Log.e(TAG, "Unexpected status code: " + response.getStatusCode());
                throw new UnexpectedStatusCodeException("Status code: " + response.getStatusCode(), statusCode);
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error", e);
            throw new UnexpectedResponseException("Error parsing JSON response");

        } finally {
            if (method != null)
                method.abort();
        }

        return result;
    }

    public void downloadFileSmart(MyAsyncTask<String, Integer, Boolean> task, String fileId, String saveToPath)
            throws IOException, NoInternetConnectionException, NotLoggedInException, UnexpectedStatusCodeException,
            UnauthorizedException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {

        if (!Utils.isNetworkConnected(context)) {
            throw new NoInternetConnectionException();
        }

        if (!isLoggedin) {
            throw new NotLoggedInException();
        }

        File file = new File(saveToPath);
        // TODO: check if we have read and write permission

        String path = String.format("/file/%s/data", fileId.toString());

//        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
//        params.add(new Pair<String, String>("file_id", fileId));
        // params.add(new Pair<String, String>("version", xxxx));
        String urlString = Utils.buildUrl(apiUrl, path, null);

        URL url = new URL(urlString);

        long startTime = System.currentTimeMillis();
        Log.i(TAG, "Downloading file: " + urlString);

        // Open a connection to that URL.
        HttpsURLConnection ucon = (HttpsURLConnection) url.openConnection();

        ucon.setRequestMethod("GET");
        ucon.addRequestProperty(FilesConstants.STACKSYNC_API, "v2");

        // this timeout affects how long it takes for the app to realize
        // there's a connection problem
        ucon.setReadTimeout(Constants.TIMEOUT_CONNECTION);

        consumer.sign(ucon);
        ucon.connect();

        int statusCode = ucon.getResponseCode();

        if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_ACCEPTED) {

            int fileSize = ucon.getContentLength();

            InputStream is = new BufferedInputStream(ucon.getInputStream());
            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
            FileOutputStream outStream = new FileOutputStream(file);
            byte[] buff = new byte[5 * 1024];

            // Read bytes (and store them) until there is nothing more to
            // read(-1)
            int len;
            int total = 0;
            int lastUpdated = 0;
            int percent = 0;

            while ((len = inStream.read(buff)) != -1) {
                total += len;
                percent = (int) (total * 100 / fileSize);
                if (lastUpdated != percent) {
                    if (task.isCancelled()) {
                        outStream.close();
                        inStream.close();
                        file.delete();
                        throw new CancelledTaskException();
                    }
                    task.setProgress(percent);
                    lastUpdated = percent;
                }

                outStream.write(buff, 0, len);
            }

            task.setProgress(100);

            // clean up
            outStream.flush();
            outStream.close();
            inStream.close();

            Log.i(TAG, "download completed in " + ((System.currentTimeMillis() - startTime) / 1000) + " sec");
        } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
            isLoggedin = false;
            throw new UnauthorizedException();
        } else {
            Log.e(TAG, "Unexpected status code: " + statusCode);
            throw new UnexpectedStatusCodeException("Status code: " + statusCode);
        }

    }

    public void uploadFileSmart(MyAsyncTask<Object, Integer, Boolean> task, Uri selectedFile, String parentId)
            throws IOException, NoInternetConnectionException, NotLoggedInException, UnexpectedStatusCodeException,
            UnauthorizedException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {

        if (!Utils.isNetworkConnected(context)) {
            throw new NoInternetConnectionException();
        }

        if (!isLoggedin) {
            throw new NotLoggedInException();
        }

        InputStream fis = context.getContentResolver().openInputStream(selectedFile);

        // TODO: do get filename and filesize in a functions.
        String fileName = "";
        long fileSize = 0;
        String scheme = selectedFile.getScheme();
        if (scheme.equals("file")) {
            File file = new File(selectedFile.getPath());
            fileName = file.getName();
            fileSize = file.length();
        } else if (scheme.equals("content")) {

            String[] proj = {"_data"};
            Cursor cursor = context.getContentResolver().query(selectedFile, proj, null, null, null);
            if (cursor.moveToFirst()) {
                File file = new File(cursor.getString(0));
                fileName = file.getName();
                fileSize = file.length();
            } else {
                throw new IOException("Can't retrieve path from uri: " + selectedFile.toString());
            }
        }

		/*
        if (fileName.length() == 0 || fileSize == 0) {
			throw new IOException("File name is empty or file size is 0.");
		}
		*/

        String path = "/file";
        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>("name", fileName));

        if (parentId != null) {
            params.add(new Pair<String, String>("parent", parentId));
        }

        String urlString = Utils.buildUrl("storageURL", path, params);

        URL url = new URL(urlString);

        long startTime = System.currentTimeMillis();
        Log.i(TAG, "Uploading file: " + urlString);

        // Open a connection to that URL.
        HttpsURLConnection ucon = (HttpsURLConnection) url.openConnection();

        ucon.setRequestMethod("POST");
        ucon.addRequestProperty(FilesConstants.STACKSYNC_API, "v2");

        // this timeout affects how long it takes for the app to realize
        // there's a connection problem
        ucon.setReadTimeout(Constants.TIMEOUT_CONNECTION);

        ucon.setDoOutput(true);

        consumer.sign(ucon);
        ucon.connect();

        OutputStream os = ucon.getOutputStream();

        BufferedInputStream bfis = new BufferedInputStream(fis);
        byte[] buffer = new byte[1024];
        int len;
        int total = 0;
        int lastUpdated = 0;
        int percent = 0;

        while ((len = bfis.read(buffer)) > 0) {
            total += len;
            percent = (int) (total * 100 / fileSize);
            if (lastUpdated != percent) {
                task.setProgress(percent);
                lastUpdated = percent;
            }

            os.write(buffer, 0, len);
        }

        task.setProgress(100);

        // clean up
        os.flush();
        os.close();
        bfis.close();

        int statusCode = ucon.getResponseCode();

        if (statusCode >= 200 && statusCode < 300) {
            Log.i(TAG, "upload completed in " + ((System.currentTimeMillis() - startTime) / 1000) + " sec");

        } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
            isLoggedin = false;
            throw new UnauthorizedException();
        } else {
            Log.e(TAG, "Unexpected status code: " + statusCode);
            throw new UnexpectedStatusCodeException("Status code: " + statusCode);
        }
    }

    public void createFolder(String folderName, String parent) throws NoInternetConnectionException,
            NotLoggedInException, UnexpectedStatusCodeException, ClientProtocolException, IOException,
            UnauthorizedException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {

        if (!Utils.isNetworkConnected(context)) {
            throw new NoInternetConnectionException();
        }

        if (!isLoggedin) {
            throw new NotLoggedInException();
        }

        HttpPost method = null;

        long startTime = System.currentTimeMillis();

        String path = "/folder";
        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>("name", folderName));
        if (parent != null) {
            params.add(new Pair<String, String>("parent", parent));
        }

        String url = Utils.buildUrl("storageURL", path, params);

        method = new HttpPost(url);
        method.getParams().setIntParameter("http.socket.timeout", Constants.TIMEOUT_CONNECTION);
        method.setHeader(FilesConstants.STACKSYNC_API, "v2");
        consumer.sign(method);


        try {
            FilesResponse response = new FilesResponse(client.execute(method));

            int statusCode = response.getStatusCode();

            if (statusCode >= 200 && statusCode < 300) {

                Log.i(TAG, "Folder created in " + ((System.currentTimeMillis() - startTime) / 1000) + " sec");
            } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                isLoggedin = false;
                throw new UnauthorizedException();
            } else {
                throw new UnexpectedStatusCodeException("Status code: " + statusCode);
            }

        } finally {
            if (method != null)
                method.abort();
        }
    }

    public void deleteFile(String fileId) throws NoInternetConnectionException, NotLoggedInException,
            ClientProtocolException, IOException, UnexpectedStatusCodeException, UnauthorizedException {

        if (!Utils.isNetworkConnected(context)) {
            throw new NoInternetConnectionException();
        }

        if (!isLoggedin) {
            throw new NotLoggedInException();
        }

        HttpDelete method = null;

        long startTime = System.currentTimeMillis();

        String path = "/stacksync/files";
        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>("file_id", fileId));

        String url = Utils.buildUrl("storageURL", path, params);

        method = new HttpDelete(url);
        method.getParams().setIntParameter("http.socket.timeout", Constants.TIMEOUT_CONNECTION);
        method.setHeader(FilesConstants.STACKSYNC_API, "v2");

        try {
            FilesResponse response = new FilesResponse(client.execute(method));
            int statusCode = response.getStatusCode();

            if (statusCode >= 200 && statusCode < 300) {

                Log.i(TAG, "File deleted in " + ((System.currentTimeMillis() - startTime) / 1000) + " sec");
            } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                isLoggedin = false;
                throw new UnauthorizedException();
            } else {
                throw new UnexpectedStatusCodeException("Status code: " + statusCode);
            }

        } finally {
            if (method != null)
                method.abort();
        }
    }

    /**
     * always verify the host - dont check for certificate
     */
    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

}
