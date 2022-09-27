package com.bindord.eureka.auth.service.impl;

import com.bindord.eureka.auth.advice.CustomValidationException;
import com.bindord.eureka.auth.domain.SpecialistPersist;
import com.bindord.eureka.auth.service.SpecialistService;
import com.bindord.eureka.auth.service.base.UserCredential;
import com.bindord.eureka.auth.wsc.KeycloakClientConfiguration;
import com.bindord.eureka.auth.wsc.ResourceServerClientConfiguration;
import com.bindord.keycloak.auth.model.UserRepresentation;
import com.bindord.resourceserver.model.Specialist;
import com.bindord.resourceserver.model.SpecialistCv;
import com.bindord.resourceserver.model.SpecialistCvDto;
import com.bindord.resourceserver.model.SpecialistDto;
import com.bindord.resourceserver.model.SpecialistSpecialization;
import com.bindord.resourceserver.model.SpecialistSpecializationDto;
import com.bindord.resourceserver.model.WorkLocation;
import com.bindord.resourceserver.model.WorkLocationDto;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SpecialistServiceImpl extends UserCredential implements SpecialistService {

    private final KeycloakClientConfiguration keycloakClient;

    private final ResourceServerClientConfiguration resourceServerClientConfiguration;

    @Override
    public Mono<Specialist> save(SpecialistPersist specialist) {
        return this.doValidateIfDocumentExists(specialist.getDocument()).flatMap(exist -> {
                    if (exist) {
                        return Mono.error(new CustomValidationException("Document already registered"));
                    }
                    return Mono.empty();
                })
                .then(this.doRegisterUser(keycloakClient,
                        specialist.getEmail(),
                        specialist.getPassword()
                ).flatMap(userRepresentation -> {
                    assert userRepresentation.getId() != null;
                    specialist.setId(UUID.fromString(userRepresentation.getId()));
                    return doRegisterSpecialist(specialist).flatMap(
                            spe -> doRegisterSpecialistSpecializations(specialist.getSpecialistSpecializations(), spe)
                                    .zipWith(doRegisterWorkLocations(specialist.getWorkLocations(), spe))
                                    .zipWith(doRegisterSpecialistCv(specialist.getSpecialistCv(), spe))
                                    .then(Mono.just(spe))
                    );
                }));
    }

    private Mono<Boolean> doValidateIfDocumentExists(String document) {
        return resourceServerClientConfiguration.init()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/specialist/by/query")
                        .queryParam("document", document)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Boolean.class)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @SneakyThrows
    private Mono<Specialist> doRegisterSpecialist(SpecialistDto specialist) {
        return resourceServerClientConfiguration.init()
                .post()
                .uri("/specialist")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(specialist), SpecialistDto.class)
                .retrieve()
                .bodyToMono(Specialist.class)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @SneakyThrows
    private Flux<SpecialistSpecialization> doRegisterSpecialistSpecializations(
            List<SpecialistSpecializationDto> specialistSpecializationDtos, Specialist specialist) {
        return resourceServerClientConfiguration.init()
                .post()
                .uri("/specialist-specialization/list")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Flux.fromIterable(
                        specialistSpecializationDtos.stream().map(e -> {
                                    e.setSpecialistId(specialist.getId());
                                    return e;
                                })
                                .collect(Collectors.toList())
                ), new ParameterizedTypeReference<>() {
                })
                .retrieve()
                .bodyToFlux(SpecialistSpecialization.class)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @SneakyThrows
    private Flux<WorkLocation> doRegisterWorkLocations(
            List<WorkLocationDto> workLocationDtos, Specialist specialist) {
        return resourceServerClientConfiguration.init()
                .post()
                .uri("/work-location/list")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Flux.fromIterable(
                        workLocationDtos.stream().map(e -> {
                                    e.setSpecialistId(specialist.getId());
                                    return e;
                                })
                                .collect(Collectors.toList())
                ), new ParameterizedTypeReference<>() {
                })
                .retrieve()
                .bodyToFlux(WorkLocation.class)
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<SpecialistCv> doRegisterSpecialistCv(SpecialistCvDto specialistCvDto, Specialist specialist) {
        specialistCvDto.setId(specialist.getId());
        return resourceServerClientConfiguration.init()
                .post()
                .uri("/specialist-cv")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(specialistCvDto), SpecialistCvDto.class)
                .retrieve()
                .bodyToMono(SpecialistCv.class)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
