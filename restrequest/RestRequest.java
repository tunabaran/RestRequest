package com.tunabaranurut.restrequest;

import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tunabaranurut.restrequest.Interceptors.PostExecuteInterceptor;
import com.tunabaranurut.restrequest.Interceptors.PreExecuteInterceptor;
import com.tunabaranurut.restrequest.callbacks.OnRequestCancelCallback;
import com.tunabaranurut.restrequest.callbacks.OnRequestFailedCallback;
import com.tunabaranurut.restrequest.callbacks.OnRequestRetryCallback;
import com.tunabaranurut.restrequest.callbacks.OnRequestSuccessCallback;
import com.tunabaranurut.restrequest.callbacks.RestRequestCallback;
import com.tunabaranurut.restrequest.http.AsyncRequestTask;
import com.tunabaranurut.restrequest.http.AuthorizationHeader;
import com.tunabaranurut.restrequest.http.HttpRequestException;
import com.tunabaranurut.restrequest.http.HttpRequester;
import com.tunabaranurut.restrequest.http.HttpRequesterImpl;
import com.tunabaranurut.restrequest.http.RequestHeader;
import com.tunabaranurut.restrequest.request.RequestStatus;
import com.tunabaranurut.restrequest.request.RequestType;
import com.tunabaranurut.restrequest.request.Requester;
import com.tunabaranurut.restrequest.response.ApiResponse;
import com.tunabaranurut.restrequest.response.ResponseStatus;

import java.io.InterruptedIOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tunabaranurut on 8.06.2018.
 */

public class RestRequest {

    private static String TAG = RestRequest.class.getSimpleName();

    private RequestStatus requestStatus;

    private RestRequestCallback callback;
    private Requester requester;
    private HttpRequester httpRequester;

    private List<RequestHeader> headers;

    private String requestUrl;

    private boolean async;
    private boolean overrideAsync = false;

    private int retryCount = -1;

    private OnRequestSuccessCallback onRequestSuccessCallback;
    private OnRequestFailedCallback onRequestFailedCallback;
    private OnRequestCancelCallback onRequestCancelCallback;
    private OnRequestRetryCallback onRequestRetryCallback;

    private PreExecuteInterceptor preExecuteInterceptor;
    private PostExecuteInterceptor postExecuteInterceptor;

    public RestRequest(String requestUrl) {
        this.requestUrl = requestUrl;

        init();
    }

    public RestRequest(String requestUrl, RestRequestCallback callback) {
        this.callback = callback;
        this.requestUrl = requestUrl;

        init();
    }

    private void init(){
        this.httpRequester = new HttpRequesterImpl();
        this.requestStatus = RequestStatus.RAW;

        if(GlobalRestConfig.DEBUG){
            Log.i(TAG, "(DEBUG)RestRequest: Created new RestRequest " + this.toString());
        }
    }

    public <T> void exchange(final Object request, final RequestType requestType, final Class<? extends T> responseType){
        if(request == null && requestType == RequestType.POST){
            return;
        }

        if(GlobalRestConfig.DEBUG){
            Log.i(TAG, "(DEBUG)RestRequest: exchanging request, Type = " + requestType + " ResponseClass = " + responseType.getSimpleName() + "," + this.toString());
        }

        prepareHeaders();

        final ObjectMapper objectMapper = new ObjectMapper();

//        try {

            requester = new Requester() {
                @Override
                public ApiResponse performRequest() {
                    ApiResponse apiResponse = new ApiResponse();

                    try {
                        String reqJson = objectMapper.writeValueAsString(request);

                        if(GlobalRestConfig.preExecuteInterceptor != null) {
                            reqJson = GlobalRestConfig.preExecuteInterceptor.onPreExecute(reqJson);
                            if(GlobalRestConfig.DEBUG){
                                Log.i(TAG, "(DEBUG)RestRequest: Global onPreExecute, request payload = " + reqJson + "," + this.toString());
                            }
                        }

                        if(preExecuteInterceptor != null) {
                            reqJson = preExecuteInterceptor.onPreExecute(reqJson);
                            if(GlobalRestConfig.DEBUG){
                                Log.i(TAG, "(DEBUG)RestRequest: onPreExecute, request payload = " + reqJson + "," + this.toString());
                            }
                        }

                        String respJson;
                        switch (requestType){
                            case GET:
                                respJson = httpRequester.sendGetRequest(requestUrl, headers);
                                break;
                            case POST:
                                respJson = httpRequester.sendPostRequest(requestUrl, reqJson, headers);
                                break;
                            default:
                                throw new Exception("Unsupported Request Type");
                        }

                        if(GlobalRestConfig.postExecuteInterceptor != null) {
                            respJson = GlobalRestConfig.postExecuteInterceptor.onPostExecute(respJson);
                            if(GlobalRestConfig.DEBUG){
                                Log.i(TAG, "(DEBUG)RestRequest: Global onPostExecute, request response = " + respJson + "," + this.toString());
                            }
                        }

                        if(postExecuteInterceptor != null){
                            respJson = postExecuteInterceptor.onPostExecute(respJson);
                            if(GlobalRestConfig.DEBUG){
                                Log.i(TAG, "(DEBUG)RestRequest: onPostExecute, response = " + respJson+ "," + this.toString());
                            }
                        }

                        T data = objectMapper.readValue(respJson, responseType);
                        apiResponse.setData(data);
                        apiResponse.setResponseStatus(ResponseStatus.SUCCESS);
                    } catch (InterruptedIOException e){
//                        e.printStackTrace();
                        apiResponse.setResponseStatus(ResponseStatus.CANCELLED);
                    } catch (Exception e) {
                        apiResponse.setResponseStatus(ResponseStatus.FAILED);

                        if(e instanceof HttpRequestException) {
                            if(((HttpRequestException) e).getResponseCode().equals("INT1")){
                                apiResponse.setResponseStatus(ResponseStatus.CANCELLED);
                            }

                            if(GlobalRestConfig.DEBUG){
                                Log.e(TAG, "(DEBUG)RestRequest: HttpRequestException responseCode = " + ((HttpRequestException) e).getResponseCode() + ", "+ this.toString());
                            }
                        }
                    }
                    requestStatus = RequestStatus.SENT;

                    return apiResponse;
                }
            };
//        }catch (Exception e){
//            throw new RuntimeException();
//        }

        send();
    }

    private void prepareHeaders(){
        if(this.headers == null){
            this.headers = new LinkedList<>();
        }


        if(GlobalRestConfig.authorizationHeader != null){
            if(!headers.contains(GlobalRestConfig.authorizationHeader)){
                headers.add(GlobalRestConfig.authorizationHeader);
                if(GlobalRestConfig.DEBUG){
                    Log.i(TAG, "(DEBUG)RestRequest: Added header " + this.toString());
                }
            }
        }

        if(GlobalRestConfig.requestHeaders != null) {
            for (RequestHeader header : GlobalRestConfig.requestHeaders) {
                if (!headers.contains(header)) {
                    headers.add(header);
                    if(GlobalRestConfig.DEBUG){
                        Log.i(TAG, "(DEBUG)RestRequest: Added header, name : " + header.getName() + " value : " + header.getValue() + "," + this.toString());
                    }
                }
            }
        }
    }

    private void send(){
        if(retryCount == -1){
            retryCount = GlobalRestConfig.retryCount;
        }
        AsyncRequestTask asyncRequestTask = new AsyncRequestTask(this,callback);

        boolean isAsync = overrideAsync ? async : GlobalRestConfig.async;

        if(isAsync){
            asyncRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else{
            asyncRequestTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }

    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.overrideAsync = true;
        this.async = async;
        if(GlobalRestConfig.DEBUG){
            Log.i(TAG, "(DEBUG)RestRequest: setAsync = " + async + ", " + this.toString());
        }
    }

    public boolean isOverrideAsync() {
        return overrideAsync;
    }

    public void addHeader(RequestHeader header){
        if(this.headers == null){
            this.headers = new LinkedList<>();
        }

        this.headers.add(header);
        if(GlobalRestConfig.DEBUG){
            Log.i(TAG, "(DEBUG)RestRequest: Added header, name : " + header.getName() + " value : " + header.getValue() + "," + this.toString());
        }

    }

    public void setbasicAuth(String username, String password){
        addHeader(new AuthorizationHeader(username,password));
    }

    public List<RequestHeader> getHeaders(){
        return headers;
    }

    public void setHeaders(List<RequestHeader> headers) {
        this.headers = headers;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public Requester getRequester() {
        return requester;
    }

    public void setRetryCount(int retryCount){
        this.retryCount = retryCount;
        if(GlobalRestConfig.DEBUG){
            Log.i(TAG, "(DEBUG)RestRequest: setRetryCount = " + retryCount + ", " + this.toString());
        }
    }

    public int getRetryCount() {
        return retryCount;
    }

    public OnRequestSuccessCallback getOnRequestSuccessCallback() {
        return onRequestSuccessCallback;
    }

    public void setOnRequestSuccessCallback(OnRequestSuccessCallback onRequestSuccessCallback) {
        this.onRequestSuccessCallback = onRequestSuccessCallback;
        if(GlobalRestConfig.DEBUG) {
            Log.i(TAG, "(DEBUG)RestRequest: setOnRequestSuccessCallback = " + this.toString());
        }
    }

    public OnRequestFailedCallback getOnRequestFailedCallback() {
        return onRequestFailedCallback;
    }

    public void setOnRequestFailedCallback(OnRequestFailedCallback onRequestFailedCallback) {
        this.onRequestFailedCallback = onRequestFailedCallback;
        if(GlobalRestConfig.DEBUG) {
            Log.i(TAG, "(DEBUG)RestRequest: setOnRequestFailedCallback = " + this.toString());
        }
    }

    public OnRequestCancelCallback getOnRequestCancelCallback() {
        return onRequestCancelCallback;
    }

    public void setOnRequestCancelCallback(OnRequestCancelCallback onRequestCancelCallback) {
        this.onRequestCancelCallback = onRequestCancelCallback;
        if(GlobalRestConfig.DEBUG) {
            Log.i(TAG, "(DEBUG)RestRequest: setOnRequestCancelCallback = " + this.toString());
        }
    }

    public OnRequestRetryCallback getOnRequestRetryCallback() {
        return onRequestRetryCallback;
    }

    public void setOnRequestRetryCallback(OnRequestRetryCallback onRequestRetryCallback) {
        this.onRequestRetryCallback = onRequestRetryCallback;
        if(GlobalRestConfig.DEBUG) {
            Log.i(TAG, "(DEBUG)RestRequest: setOnRequestRetryCallback = " + this.toString());
        }
    }

    public PreExecuteInterceptor getPreExecuteInterceptor() {
        return preExecuteInterceptor;
    }

    public void setPreExecuteInterceptor(PreExecuteInterceptor preExecuteInterceptor) {
        this.preExecuteInterceptor = preExecuteInterceptor;
        if(GlobalRestConfig.DEBUG) {
            Log.i(TAG, "(DEBUG)RestRequest: setPreExecuteInterceptor = " + this.toString());
        }
    }

    public PostExecuteInterceptor getPostExecuteInterceptor() {
        return postExecuteInterceptor;
    }

    public void setPostExecuteInterceptor(PostExecuteInterceptor postExecuteInterceptor) {
        this.postExecuteInterceptor = postExecuteInterceptor;
        if(GlobalRestConfig.DEBUG) {
            Log.i(TAG, "(DEBUG)RestRequest: setPostExecuteInterceptor = " + this.toString());
        }
    }
}
