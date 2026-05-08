package com.guzem.uzaktan.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AjaxResult {
    private boolean success;
    private String message;
    private String type;
    private Object data;

    public AjaxResult() {}

    public AjaxResult(boolean success, String message, String type) {
        this.success = success;
        this.message = message;
        this.type = type;
    }

    public AjaxResult(boolean success, String message, String type, Object data) {
        this.success = success;
        this.message = message;
        this.type = type;
        this.data = data;
    }

    public static AjaxResult success(String message) {
        return new AjaxResult(true, message, "success");
    }

    public static AjaxResult success(String message, Object data) {
        return new AjaxResult(true, message, "success", data);
    }

    public static AjaxResult error(String message) {
        return new AjaxResult(false, message, "error");
    }

    public static AjaxResult error(String message, Object data) {
        return new AjaxResult(false, message, "error", data);
    }

    public static AjaxResult warn(String message) {
        return new AjaxResult(false, message, "warning");
    }

    public static AjaxResult info(String message) {
        return new AjaxResult(false, message, "info");
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
