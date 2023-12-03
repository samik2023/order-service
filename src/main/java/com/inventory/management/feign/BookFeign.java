package com.inventory.management.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "book-service", url = "${book.service.url}")
    public interface BookFeign {
        @GetMapping(value = "/books/{id}" ,consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
        String getProductById(@PathVariable Long id);
    }

