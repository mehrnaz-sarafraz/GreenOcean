package com.greenocean.backend.auth.config;

import com.greenocean.backend.common.api.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException
    ) throws IOException {
        HttpStatus status = temporarilyUnavailable(authenticationException) ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.UNAUTHORIZED;
        response.setStatus(status.value());
        response.setHeader("Cache-Control","no-store");
        if(status==HttpStatus.SERVICE_UNAVAILABLE)response.setHeader("Retry-After","5");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ApiError(
                Instant.now(), status.value(), status.getReasonPhrase(),
                status==HttpStatus.SERVICE_UNAVAILABLE?"Authentication verification is temporarily unavailable. Please retry.":"Authentication is required", request.getRequestURI(), List.of()
        ));
    }
    private boolean temporarilyUnavailable(Throwable failure){
        for(int depth=0;failure!=null&&depth<8;depth++,failure=failure.getCause()){
            if(failure instanceof org.springframework.security.oauth2.jwt.JwtValidationException validation)
                return !validation.getErrors().isEmpty()&&validation.getErrors().stream()
                    .allMatch(error->"temporarily_unavailable".equals(error.getErrorCode()));
        }
        return false;
    }
}
