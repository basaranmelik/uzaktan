package com.guzem.uzaktan.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionResult {

    private final boolean success;
    private final String message;
    private final String redirectUrl;
    private final Object data;

    private ActionResult(boolean success, String message, String redirectUrl, Object data) {
        this.success = success;
        this.message = message;
        this.redirectUrl = redirectUrl;
        this.data = data;
    }

    public static ActionResult success(String message) {
        return new ActionResult(true, message, null, null);
    }

    public static ActionResult success(String message, String redirectUrl) {
        return new ActionResult(true, message, redirectUrl, null);
    }

    public static ActionResult error(String message) {
        return new ActionResult(false, message, null, null);
    }

    public static ActionResult successWithData(String message, Map<String, Object> data) {
        return new ActionResult(true, message, null, data);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getRedirectUrl() { return redirectUrl; }
    public Object getData() { return data; }
}
