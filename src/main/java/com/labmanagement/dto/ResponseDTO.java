package com.labmanagement.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO<T> {

    private final int status;
    private final LocalDateTime dateTime;
    private final T data;
    private final String error;

    private ResponseDTO(int status, T data, String error) {
        this.status = status;
        this.dateTime = LocalDateTime.now();
        this.data = data;
        this.error = error;
    }

    public static ResponseDTO<Void> success() {
        return new ResponseDTO<>(1, null, null);
    }

    public static <T> ResponseDTO<T> success(T data) {
        return new ResponseDTO<>(1, data, null);
    }

    public static ResponseDTO<Void> failure(String error) {
        return new ResponseDTO<>(0, null, error);
    }

    public int getStatus() {
        return status;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }
}