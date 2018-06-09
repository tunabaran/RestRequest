package com.tunabaranurut.restrequest.http;

/**
 * Created by tunabaranurut on 8.06.2018.
 */

public class HttpRequestException extends Exception{

    private String responseCode;
    private String responseMessage;

    public HttpRequestException(String responseCode, String responseMessage) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }

    public String getResponseCode(){
        return responseCode;
    }

    public String getResponseMessage(){
        return responseMessage;
    }
}
