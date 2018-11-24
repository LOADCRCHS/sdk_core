package com.ssm.sdk.exception;

public class SdkException extends Exception {
    private String name;
    private String message;

    public SdkException(){
        super();
    }

    public SdkException(String message){
        //super(message);
        System.out.println(message);
        this.name=message;
        this.message = message;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
