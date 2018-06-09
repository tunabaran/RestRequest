package com.tunabaranurut.restrequest.request;

/**
 * Created by tunabaranurut on 8.06.2018.
 */

public enum RequestStatus {
    RAW(0),
    READY(1),
    SENT(2);

    private final int index;

    RequestStatus(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static RequestStatus getRequestStatus(int index){
        switch (index){
            case 0: return RAW;
            case 1: return READY;
            case 2: return SENT;

        }
        return null;
    }
}
