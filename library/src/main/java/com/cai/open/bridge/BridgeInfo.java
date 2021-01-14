package com.cai.open.bridge;

public class BridgeInfo {

    public String implClass;
    public boolean isMock;

    public BridgeInfo(String implClass, boolean isMock) {
        this.implClass = implClass;
        this.isMock = isMock;
    }

    @Override
    public String toString() {
        return "{ impl:" + implClass + ", isMock: " + isMock + " }";
    }
}
