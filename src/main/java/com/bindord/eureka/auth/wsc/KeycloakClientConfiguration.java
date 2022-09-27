package com.bindord.eureka.auth.wsc;

import com.bindord.eureka.auth.configuration.props.ClientProperties;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@AllArgsConstructor
public class KeycloakClientConfiguration extends BaseClientConfiguration {


    private ClientProperties clientProperties;

    private WebClient.Builder webClientBuilder;

    public WebClient init() {
        ClientProperties.ClientConfig config = clientProperties.getKeycloakAuth();
        ClientHttpConnector connector = this.instanceBaseConfig(config);
        return webClientBuilder
                .baseUrl(config.getUrl())
                .clientConnector(connector)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
