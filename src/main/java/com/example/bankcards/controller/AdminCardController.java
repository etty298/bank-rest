package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CreateCardRequest;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {
    private final CardService cardService;

    public AdminCardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<CardResponse> create(@Valid @RequestBody CreateCardRequest request) {
        return ResponseEntity.ok(cardService.createForUser(request));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<CardResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.activate(id));
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<CardResponse> block(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.block(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(cardService.getAll(pageable));
    }
}



