package com.example.transaction_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;

@FeignClient(name = "ACCOUNT-SERVICE", path = "/api/accounts")
public interface AccountClient {

    @PostMapping("/{id}/deposit")
    ResponseEntity<Object> deposit(@PathVariable("id") Long id, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/{id}/withdraw")
    ResponseEntity<Object> withdraw(@PathVariable("id") Long id, @RequestParam("amount") BigDecimal amount);
}
