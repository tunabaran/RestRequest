package com.tunabaranurut.restrequest.http;

import android.util.Base64;

/**
 * Created by tunabaranurut on 9.06.2018.
 */

public class AuthorizationHeader extends RequestHeader {

    public AuthorizationHeader(String username, String password) {
        super("Authorization", "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT));

    }

    private String getBase64Value(String username, String password){
        String auth = username + ":" + password;
        String base64 = Base64.encodeToString(auth.getBytes(), Base64.DEFAULT);
        return "Basic " + base64;

    }
}
