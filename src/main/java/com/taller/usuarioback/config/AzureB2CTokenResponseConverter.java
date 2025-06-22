package com.taller.usuarioback.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AzureB2CTokenResponseConverter implements Converter<Map<String, Object>, OAuth2AccessTokenResponse> {

    @Override
    public OAuth2AccessTokenResponse convert(Map<String, Object> tokenResponseParameters) {
        System.out.println("=== Azure B2C Token Response Converter ===");
        System.out.println("Raw token response parameters: " + tokenResponseParameters);
        
        String accessToken = getParameterValue(tokenResponseParameters, OAuth2ParameterNames.ACCESS_TOKEN);
        System.out.println("Access token: " + (accessToken != null ? accessToken.substring(0, Math.min(20, accessToken.length())) + "..." : "null"));
        
        OAuth2AccessToken.TokenType accessTokenType = getAccessTokenType(tokenResponseParameters);
        long expiresIn = getExpiresIn(tokenResponseParameters);
        String refreshToken = getParameterValue(tokenResponseParameters, OAuth2ParameterNames.REFRESH_TOKEN);
        String scope = getParameterValue(tokenResponseParameters, OAuth2ParameterNames.SCOPE);
        String idToken = getParameterValue(tokenResponseParameters, "id_token");

        System.out.println("Token type: " + accessTokenType);
        System.out.println("Expires in: " + expiresIn);
        System.out.println("Scope: " + scope);
        System.out.println("ID token present: " + (idToken != null));

        if (!StringUtils.hasText(accessToken)) {
            System.err.println("ERROR: Access token is null or empty!");
            if(StringUtils.hasText(idToken)) {
                accessToken = "dummy-access-token-for-b2c";
            } else {
                throw new IllegalArgumentException("Access token and ID token are null or empty");
            }
        }

        OAuth2AccessTokenResponse.Builder builder = OAuth2AccessTokenResponse.withToken(accessToken)
                .tokenType(accessTokenType)
                .expiresIn(expiresIn);

        if (StringUtils.hasText(refreshToken)) {
            builder.refreshToken(refreshToken);
        }

        if (StringUtils.hasText(scope)) {
            Set<String> scopes = new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
            builder.scopes(scopes);
        }

        if (StringUtils.hasText(idToken)) {
            builder.additionalParameters(Map.of("id_token", idToken));
        }

        OAuth2AccessTokenResponse response = builder.build();
        System.out.println("Successfully built OAuth2AccessTokenResponse");
        return response;
    }

    private static String getParameterValue(Map<String, Object> parameters, String parameterName) {
        Object value = parameters.get(parameterName);
        return (value != null) ? value.toString() : null;
    }

    private static OAuth2AccessToken.TokenType getAccessTokenType(Map<String, Object> parameters) {
        String tokenType = getParameterValue(parameters, OAuth2ParameterNames.TOKEN_TYPE);
        if (StringUtils.hasText(tokenType) && tokenType.equalsIgnoreCase("Bearer")) {
            return OAuth2AccessToken.TokenType.BEARER;
        }
        return OAuth2AccessToken.TokenType.BEARER;
    }

    private static long getExpiresIn(Map<String, Object> parameters) {
        String expiresIn = getParameterValue(parameters, OAuth2ParameterNames.EXPIRES_IN);
        if (StringUtils.hasText(expiresIn)) {
            try {
                return Long.parseLong(expiresIn);
            } catch (NumberFormatException ex) {
                return 0L;
            }
        }
        return 0L;
    }
} 