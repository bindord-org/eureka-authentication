package com.bindord.eureka.auth;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.function.Consumer;

public class ThatsWhyWePlay {

    public static void main(String[] args) throws InterruptedException {
        Consumer<Integer> consumer = s -> System.out.println(s + " : " + Thread.currentThread().getName());

        Flux.range(1, 5)
                .publishOn(Schedulers.newElastic("First_PublishOn()_thread"))
                .doOnNext(consumer)
                .map(i -> {
                    System.out.println("Inside map the thread is " + Thread.currentThread().getName());
                    return i * 10;
                })
                .publishOn(Schedulers.newElastic("First_PublishOn()_thread"))
                .doOnNext(consumer)
                .publishOn(Schedulers.newElastic("Second_PublishOn()_thread"))
                .doOnNext(consumer)
                .subscribeOn(Schedulers.newElastic("subscribeOn_thread"))
                .subscribe();
    }

}
