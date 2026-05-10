package com.guzem.uzaktan.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guzem.uzaktan.dto.response.ActionResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class ActionResultHandler implements HandlerMethodReturnValueHandler {

    private final ObjectMapper objectMapper;

    public ActionResultHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return ActionResult.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        ActionResult result = (ActionResult) returnValue;
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);

        mavContainer.setRequestHandled(true);

        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        if (isAjax) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", result.isSuccess());
            if (result.getMessage() != null) body.put("message", result.getMessage());
            if (result.getData() != null) body.put("data", result.getData());
            objectMapper.writeValue(response.getWriter(), body);
        } else {
            if (result.getRedirectUrl() != null) {
                FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
                if (flashMap != null) {
                    String key = result.isSuccess() ? "successMessage" : "errorMessage";
                    flashMap.put(key, result.getMessage());
                }
                response.sendRedirect(result.getRedirectUrl());
            }
        }
    }
}
