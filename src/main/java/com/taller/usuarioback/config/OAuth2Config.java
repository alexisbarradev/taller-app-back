package com.taller.usuarioback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.web.client.RestTemplate;

import org.springframework.beans.factory.annotation.Value;


import java.util.Arrays;

@Configuration
public class OAuth2Config {
    @Value("${azure.b2c.client-id}")
    private String azureClientId;

    @Value("${azure.b2c.client-secret}")
    private String azureClientSecret;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(azureB2CClientRegistration());
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient tokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();

        OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter =
                new OAuth2AccessTokenResponseHttpMessageConverter();
        tokenResponseHttpMessageConverter.setAccessTokenResponseConverter(new AzureB2CTokenResponseConverter());

        RestTemplate restTemplate = new RestTemplate(Arrays.asList(
                new FormHttpMessageConverter(), tokenResponseHttpMessageConverter));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

        tokenResponseClient.setRestOperations(restTemplate);
        return tokenResponseClient;
    }

    /*for commit porpuses delete secret and save it */
    private ClientRegistration azureB2CClientRegistration() {
        return ClientRegistration.withRegistrationId("B2C_1_DuocUCDemoAzure_Login")
                .clientId(azureClientId)
                .clientSecret(azureClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("https://proyectouc.b2clogin.com/proyectouc.onmicrosoft.com/B2C_1_DuocUCDemoAzure_Login/oauth2/v2.0/authorize")
                .tokenUri("https://proyectouc.b2clogin.com/proyectouc.onmicrosoft.com/B2C_1_DuocUCDemoAzure_Login/oauth2/v2.0/token")
                .jwkSetUri("https://proyectouc.b2clogin.com/proyectouc.onmicrosoft.com/B2C_1_DuocUCDemoAzure_Login/discovery/v2.0/keys")
                .userInfoUri("https://proyectouc.b2clogin.com/proyectouc.onmicrosoft.com/B2C_1_DuocUCDemoAzure_Login/openid/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .clientName("Azure B2C")
                .build();
    }
} 