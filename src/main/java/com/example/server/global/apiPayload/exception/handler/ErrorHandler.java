package com.example.server.global.apiPayload.exception.handler;


import com.example.server.global.apiPayload.code.BaseErrorCode;
import com.example.server.global.apiPayload.exception.GeneralException;

public class ErrorHandler extends GeneralException {

    public ErrorHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }

}
