package com.tunabaranurut.restrequest.http;

import java.util.List;

/**
 * Created by tunabaranurut on 8.06.2018.
 */

public interface HttpRequester {

    String sendPostRequestWithAuth(String requestUrl, String payload, String username, String password) throws HttpRequestException;

    String sendPostRequest(String requestUrl, String payload, RequestHeader header) throws HttpRequestException;

    String sendPostRequest(String requestUrl, String payload) throws HttpRequestException;

    String sendPostRequest(String requestUrl) throws HttpRequestException;

    String sendPostRequest(String requestUrl, String payload, List<RequestHeader> headers) throws HttpRequestException;

    String sendGetRequest(String requestUrl, List<RequestHeader> headers) throws HttpRequestException;

    String sendGetRequest(String requestUrl) throws HttpRequestException;

    String sendGetRequest(String requestUrl, RequestHeader header) throws HttpRequestException;

    String sendGetRequestWithAuth(String requestUrl, String username, String password) throws HttpRequestException;
}
