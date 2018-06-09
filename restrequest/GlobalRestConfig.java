package com.tunabaranurut.restrequest;

import com.tunabaranurut.restrequest.Interceptors.PostExecuteInterceptor;
import com.tunabaranurut.restrequest.Interceptors.PreExecuteInterceptor;
import com.tunabaranurut.restrequest.callbacks.OnRequestCancelCallback;
import com.tunabaranurut.restrequest.callbacks.OnRequestFailedCallback;
import com.tunabaranurut.restrequest.callbacks.OnRequestRetryCallback;
import com.tunabaranurut.restrequest.callbacks.OnRequestSuccessCallback;
import com.tunabaranurut.restrequest.http.AuthorizationHeader;
import com.tunabaranurut.restrequest.http.RequestHeader;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tunabaranurut on 9.06.2018.
 */

public abstract class GlobalRestConfig {

    public static int retryCount = 0;
    public static boolean async = false;
    public static AuthorizationHeader authorizationHeader = null;
    public static boolean addTasksToQueue = false;

    public static List<RequestHeader> requestHeaders = new LinkedList<>(Arrays.asList(
            new RequestHeader("Accept","application/json"),
            new RequestHeader("Content-Type","application/json; charset=UTF-8")
    ));

    public static boolean DEBUG = false;

    public static OnRequestSuccessCallback onRequestSuccessCallback = null;
    public static OnRequestRetryCallback onRequestRetryCallback = null;
    public static OnRequestCancelCallback onRequestCancelCallback = null;
    public static OnRequestFailedCallback onRequestFailedCallback = null;

    public static PreExecuteInterceptor preExecuteInterceptor = null;
    public static PostExecuteInterceptor postExecuteInterceptor = null;
}
