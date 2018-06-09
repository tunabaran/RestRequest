package com.tunabaranurut.restrequest.http;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tunabaranurut on 8.06.2018.
 */

public class HttpRequesterImpl implements HttpRequester{

    @Override
    public String sendPostRequestWithAuth(String requestUrl, String payload, String username, String password) throws HttpRequestException{
        List<RequestHeader> reqHeaders = new LinkedList<>();
        RequestHeader requestHeader = new RequestHeader("Authorization", getAuthHeader(username,password));
        reqHeaders.add(requestHeader);
        return sendPostRequest(requestUrl,payload,reqHeaders);
    }

    @Override
    public String sendPostRequest(String requestUrl) throws HttpRequestException {
        return sendPostRequest(requestUrl, "");
    }

    @Override
    public String sendPostRequest(String requestUrl, String payload,RequestHeader header) throws HttpRequestException{
        List<RequestHeader> reqHeaders = new LinkedList<>();
        reqHeaders.add(header);
        return sendPostRequest(requestUrl,payload,reqHeaders);
    }

    @Override
    public String sendPostRequest(String requestUrl, String payload) throws HttpRequestException{
        return sendPostRequest(requestUrl,payload,new LinkedList<RequestHeader>());
    }

    @Override
    public String sendPostRequest(String requestUrl, String payload, List<RequestHeader> headers) throws HttpRequestException{
        StringBuilder jsonString = new StringBuilder();
        URL url;
        HttpURLConnection connection = null;

        try {
            url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            if(headers != null) {
                for (RequestHeader r : headers) {
                    connection.setRequestProperty(r.getName(), r.getValue());
                }
            }
            if(payload == null || payload.equals("null")) {
                payload = "{}";
            }
            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(payload);
            writer.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();
            connection.disconnect();
        }catch (InterruptedIOException e){
            throw new HttpRequestException("INT1","IO Interrupted.");
        }catch (Exception e){
            e.printStackTrace();
            String responseCode = "";
            String responseMessage = "";
            try{
                responseCode = Integer.toString(connection.getResponseCode());
                responseMessage = connection.getResponseMessage();
            }catch (Exception ex){
                // Do nothing. If we cant get responseCode and Message it means call failed before http execution;
            }
            Log.d("LOG", "sendPostRequest: responseCode = " + responseCode);
            throw new HttpRequestException(responseCode,responseMessage);
        }
        return jsonString.toString();
    }

    @Override
    public String sendGetRequest(String requestUrl,List<RequestHeader> headers) throws HttpRequestException{
        StringBuilder jsonString = new StringBuilder();
        URL url;
        HttpURLConnection connection = null;

        try {
            url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            if(headers != null){
                for(RequestHeader r : headers){
                    connection.setRequestProperty(r.getName(), r.getValue());
                }
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();
            connection.disconnect();
        }catch (Exception e){
            String responseCode = "";
            String responseMessage = "";
            try{
                responseCode = Integer.toString(connection.getResponseCode());
                responseMessage = connection.getResponseMessage();
            }catch (Exception ex){
                // Do nothing. If we cant get responseCode and Message it means call failed before http execution;
            }
            throw new HttpRequestException(responseCode,responseMessage);
        }
        return jsonString.toString();
    }

    @Override
    public String sendGetRequest(String requestUrl) throws HttpRequestException{
        return sendGetRequest(requestUrl,new LinkedList<RequestHeader>());
    }

    @Override
    public String sendGetRequest(String requestUrl, RequestHeader header) throws HttpRequestException{
        List<RequestHeader> reqHeaders = new LinkedList<>();
        reqHeaders.add(header);
        return sendGetRequest(requestUrl,reqHeaders);
    }

    @Override
    public String sendGetRequestWithAuth(String requestUrl, String username, String password) throws HttpRequestException{
        List<RequestHeader> reqHeaders = new LinkedList<>();
        RequestHeader requestHeader = new RequestHeader("Authorization", getAuthHeader(username,password));
        reqHeaders.add(requestHeader);
        return sendGetRequest(requestUrl,reqHeaders);
    }

    public String getAuthHeader(String username, String password){
        String auth = username + ":" + password;
        String base64 = Base64.encodeToString(auth.getBytes(), Base64.DEFAULT);
        return "Basic " + base64;
    }
}
