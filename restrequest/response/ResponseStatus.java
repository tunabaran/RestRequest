package com.tunabaranurut.restrequest.response;

/**
 * Created by tunabaranurut on 8.06.2018.
 */

public enum ResponseStatus {

    RAW(0),
    SUCCESS(1),
    FAILED(2),
    CANCELLED(3);

    private final int index;

    ResponseStatus(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static ResponseStatus getResponseStatus(int index){
        switch (index){
            case 0: return RAW;
            case 1: return SUCCESS;
            case 2: return FAILED;
            case 3: return CANCELLED;

        }
        return null;
    }
}
