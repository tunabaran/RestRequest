package com.tunabaranurut.restrequest.http;

import android.os.AsyncTask;
import android.util.Log;

import com.tunabaranurut.restrequest.AsyncTaskQueue;
import com.tunabaranurut.restrequest.GlobalRestConfig;
import com.tunabaranurut.restrequest.RestRequest;
import com.tunabaranurut.restrequest.callbacks.RestRequestCallback;
import com.tunabaranurut.restrequest.response.ApiResponse;
import com.tunabaranurut.restrequest.response.ResponseStatus;

/**
 * Created by tunabaranurut on 8.06.2018.
 */

public class AsyncRequestTask extends AsyncTask<Void, Void, ApiResponse> {

    private static String TAG = AsyncRequestTask.class.getSimpleName();

    private RestRequest restRequest;
    private RestRequestCallback callback;

    private int retryLeft;

    public AsyncRequestTask(RestRequest restRequest) {
        this.restRequest = restRequest;
        this.retryLeft = restRequest.getRetryCount();

        init();
    }

    public AsyncRequestTask(RestRequest restRequest,RestRequestCallback callback) {
        this.restRequest = restRequest;
        this.callback = callback;
        this.retryLeft = restRequest.getRetryCount();

        init();
    }
    // Contructor for retry calls.
    public AsyncRequestTask(RestRequest restRequest, RestRequestCallback callback, int retryCount) {
        this.restRequest = restRequest;
        this.callback = callback;
        this.retryLeft = retryCount;

        init();
    }

    private void init(){
        if(GlobalRestConfig.addTasksToQueue){
            AsyncTaskQueue.getInstance().add(this);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ApiResponse doInBackground(Void... params) {
        if(GlobalRestConfig.DEBUG){
            Log.i(TAG, "(DEBUG)RestRequest: PerformingRequest ," + restRequest.toString());
        }
        if(!isCancelled()) {
            return restRequest.getRequester().performRequest();
        }else{
            return new ApiResponse(ResponseStatus.CANCELLED);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled(new ApiResponse(ResponseStatus.CANCELLED));
    }

    @Override
    protected void onCancelled(ApiResponse apiResponse) {

        if(GlobalRestConfig.DEBUG){
            Log.i(TAG, "(DEBUG)RestRequest: onCancelled," + restRequest.toString());
        }

        if(callback != null) {
            callback.onCancel();
        }

        if(GlobalRestConfig.onRequestCancelCallback != null){
            GlobalRestConfig.onRequestCancelCallback.onCancel();
        }

        if(restRequest.getOnRequestCancelCallback() != null){
            restRequest.getOnRequestCancelCallback().onCancel();
        }

        if(GlobalRestConfig.addTasksToQueue){
            AsyncTaskQueue.getInstance().remove(this);
        }
    }

    @Override
    protected void onPostExecute(ApiResponse apiResponse) {
        super.onPostExecute(apiResponse);

        switch (apiResponse.getResponseStatus()) {
            case SUCCESS:
                if(callback != null) {
                    callback.onSuccess(apiResponse);
                }

                if(GlobalRestConfig.onRequestSuccessCallback != null){
                    GlobalRestConfig.onRequestSuccessCallback.onSuccess(apiResponse);
                }

                if(restRequest.getOnRequestSuccessCallback() != null){
                    restRequest.getOnRequestSuccessCallback().onSuccess(apiResponse);
                }
                if(GlobalRestConfig.DEBUG){
                    Log.i(TAG, "(DEBUG)RestRequest: onSuccess ," + restRequest.toString());
                }
                break;
            case FAILED:
                if (this.retryLeft > 0) {
                    if(GlobalRestConfig.DEBUG){
                        Log.i(TAG, "(DEBUG)RestRequest: onRetry," + restRequest.toString());
                    }

                    if(callback != null) {
                        callback.onRetry(retryLeft - 1);
                    }
                    if(GlobalRestConfig.onRequestRetryCallback != null){
                        GlobalRestConfig.onRequestRetryCallback.onRetry(retryLeft - 1);
                    }

                    if(restRequest.getOnRequestRetryCallback() != null){
                        restRequest.getOnRequestRetryCallback().onRetry(retryLeft - 1);
                    }

                    AsyncRequestTask asyncRequestTask =
                            new AsyncRequestTask(restRequest, callback, this.retryLeft - 1);

                    boolean isAsync = restRequest.isOverrideAsync() ? restRequest.isAsync() : GlobalRestConfig.async;

                    if (isAsync) {
                        asyncRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        asyncRequestTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                    }

                } else {
                    if(GlobalRestConfig.DEBUG){
                        Log.i(TAG, "(DEBUG)RestRequest: onFailed," + restRequest.toString());
                    }

                    if(callback != null) {
                        callback.onFailed();
                    }

                    if(GlobalRestConfig.onRequestFailedCallback != null){
                        GlobalRestConfig.onRequestFailedCallback.onFailed();
                    }

                    if(restRequest.getOnRequestFailedCallback() != null){
                        restRequest.getOnRequestFailedCallback().onFailed();
                    }

                }
                break;
            case CANCELLED:
                if(GlobalRestConfig.DEBUG){
                    Log.i(TAG, "(DEBUG)RestRequest: onCancelled," + restRequest.toString());
                }
                if(callback != null){
                    callback.onCancel();
                }

                if(GlobalRestConfig.onRequestCancelCallback != null){
                    GlobalRestConfig.onRequestCancelCallback.onCancel();
                }

                if(restRequest.getOnRequestCancelCallback() != null){
                    restRequest.getOnRequestCancelCallback().onCancel();
                }
        }

        if(GlobalRestConfig.addTasksToQueue){
            AsyncTaskQueue.getInstance().remove(this);
        }
    }
}
