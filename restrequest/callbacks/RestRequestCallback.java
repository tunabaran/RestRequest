package com.tunabaranurut.restrequest.callbacks;


import com.tunabaranurut.restrequest.response.ApiResponse;

/**
 * Created by tunabaranurut on 8.06.2018.
 */

public interface RestRequestCallback {

    void onSuccess(ApiResponse apiResponse);

    void onFailed();

    void onRetry(int retryLeft);

    void onCancel();
}
