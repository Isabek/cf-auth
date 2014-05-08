package com.isabek;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ConnectionProvider {


    private String username = "";
    private String password = "";
    private String tta = "223";

    public void connect() {
        String url = "http://codeforces.ru/enter";
        HttpGet httpGet = new HttpGet(url);
        DefaultHttpClient httpClient = getHttpClient();

        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);

            String csrfToken = getCsrfToken(httpResponse);
            httpGet.releaseConnection();
            HttpPost httpPost = new HttpPost(url);

            List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
            parameters.add(new BasicNameValuePair("handle", username));
            parameters.add(new BasicNameValuePair("password", password));
            parameters.add(new BasicNameValuePair("csrf_token", csrfToken));
            parameters.add(new BasicNameValuePair("action", "enter"));
            parameters.add(new BasicNameValuePair("_tta", tta));

            httpPost.setEntity(new UrlEncodedFormEntity(parameters));

            httpClient.execute(httpPost);

            httpPost.releaseConnection();

            HttpGet get = new HttpGet("http://codeforces.ru/");
            HttpResponse response = httpClient.execute(get);

            StringBuffer stringBuffer = logger(response);
            System.out.println(stringBuffer.toString().trim());

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private DefaultHttpClient getHttpClient() {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        httpClient.setCredentialsProvider(provider);

        return httpClient;
    }

    private StringBuffer logger(HttpResponse httpResponse) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
        StringBuffer stringBuffer = new StringBuffer();
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            stringBuffer.append("\n").append(line);
        }
        return stringBuffer;

    }

    private String getCsrfToken(HttpResponse httpResponse) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            int position = line.indexOf("csrf_token");
            if (position > -1 && position + 51 < line.length()) {
                return line.substring(position + 19, position + 51);
            }
        }
        return null;
    }
}
