package com.tunabaranurut.restrequest.response;

/**
 * Created by tunabaranurut on 8.06.2018.
 */

public class ApiResponse {

    private ResponseStatus responseStatus = ResponseStatus.RAW;
    private Object data;

    public ApiResponse() {
    }

    public ApiResponse(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
