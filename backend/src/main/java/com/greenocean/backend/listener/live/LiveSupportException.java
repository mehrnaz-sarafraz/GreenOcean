package com.greenocean.backend.listener.live;
public class LiveSupportException extends RuntimeException {
    private final String code;
    private final int status;
    public LiveSupportException(int status,String code,String message) {super(message);this.code=code;this.status=status;}
    public String code(){return code;}
    public int status(){return status;}
}
