package com.tunabaranurut.restrequest.http;

/**
 * Created by tunabaranurut on 8.06.2018.
 */

public class RequestHeader {

    private String name;
    private String value;

    public RequestHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
