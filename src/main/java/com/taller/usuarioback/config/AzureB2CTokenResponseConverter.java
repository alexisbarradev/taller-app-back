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
        
        // Extract tokens and parameters
        String accessToken = getParameterValue(tokenResponseParameters, OAuth2ParameterNames.ACCESS_TOKEN);
        String idToken = getParameterValue(tokenResponseParameters, "id_token");
        String refreshToken = getParameterValue(tokenResponseParameters, OAuth2ParameterNames.REFRESH_TOKEN);
        String scope = getParameterValue(tokenResponseParameters, OAuth2ParameterNames.SCOPE);
        String tokenType = getParameterValue(tokenResponseParameters, OAuth2ParameterNames.TOKEN_TYPE);
        long expiresIn = getExpiresIn(tokenResponseParameters);
        
        System.out.println("Access token present: " + (accessToken != null && !accessToken.isEmpty()));
        System.out.println("ID token present: " + (idToken != null && !idToken.isEmpty()));
        System.out.println("Token type: " + tokenType);
        System.out.println("Expires in: " + expiresIn);
        System.out.println("Scope: " + scope);

        // Azure B2C specific handling
        // Some Azure B2C flows only return id_token, not access_token
        if (!StringUtils.hasText(accessToken)) {
            System.out.println("WARNING: No access token received from Azure B2C");
            if (StringUtils.hasText(idToken)) {
                System.out.println("INFO: ID token present, using it as access token for B2C flow");
                // For Azure B2C, if no access token is provided, we can use the id_token
                // This is a common pattern in B2C flows where the id_token contains all necessary claims
                accessToken = idToken;
            } else {
                System.err.println("ERROR: Neither access token nor id token received");
                throw new IllegalArgumentException("No access token or id token received from Azure B2C");
            }
        }

        // Build the OAuth2AccessTokenResponse
        OAuth2AccessTokenResponse.Builder builder = OAuth2AccessTokenResponse.withToken(accessToken)
                .tokenType(getAccessTokenType(tokenType))
                .expiresIn(expiresIn);

        // Add refresh token if present
        if (StringUtils.hasText(refreshToken)) {
            builder.refreshToken(refreshToken);
        }

        // Add scopes if present
        if (StringUtils.hasText(scope)) {
            Set<String> scopes = new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
            builder.scopes(scopes);
        }

        // Add additional parameters (including id_token if different from access_token)
        if (StringUtils.hasText(idToken) && !idToken.equals(accessToken)) {
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

    private static OAuth2AccessToken.TokenType getAccessTokenType(String tokenType) {
        if (StringUtils.hasText(tokenType) && tokenType.equalsIgnoreCase("Bearer")) {
            return OAuth2AccessToken.TokenType.BEARER;
        }
        // Default to Bearer for Azure B2C
        return OAuth2AccessToken.TokenType.BEARER;
    }

    private static long getExpiresIn(Map<String, Object> parameters) {
        String expiresIn = getParameterValue(parameters, OAuth2ParameterNames.EXPIRES_IN);
        if (StringUtils.hasText(expiresIn)) {
            try {
                return Long.parseLong(expiresIn);
            } catch (NumberFormatException ex) {
                System.err.println("WARNING: Invalid expires_in value: " + expiresIn);
                return 3600L; // Default to 1 hour
            }
        }
        return 3600L; // Default to 1 hour if not specified
    }
} 