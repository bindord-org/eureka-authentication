package com.bindord.eureka.auth.service.base;

import com.bindord.eureka.auth.wsc.KeycloakClientConfiguration;
import com.bindord.keycloak.auth.model.UserLogin;
import com.bindord.keycloak.auth.model.UserRepresentation;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class UserCredential {

    protected Mono<UserRepresentation> doRegisterUser(KeycloakClientConfiguration keycloakClient,
                                                    String email,
                                                    String pwd) {
        var userLogin = new UserLogin();
        userLogin.setEmail(email);
        userLogin.setPassword(pwd);
        return keycloakClient.init()
                .post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(userLogin), UserLogin.class)
                .retrieve()
                .bodyToMono(UserRepresentation.class)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
