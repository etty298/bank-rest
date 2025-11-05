package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getMyCards(Pageable pageable) {
        return ResponseEntity.ok(cardService.getOwn(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getMyCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getOwn(id));
    }

    @GetMapping("/balance/{id}")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getBalance(id));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        cardService.transfer(request);
        return ResponseEntity.ok().build();
    }
}



