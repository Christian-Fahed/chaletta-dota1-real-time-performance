package com.chaletta.chalettaperformance.controller;

import com.chaletta.chalettaperformance.model.TitleDefinition;
import com.chaletta.chalettaperformance.service.TitleDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/title-definitions")
@RequiredArgsConstructor
public class TitleDefinitionController {

    private final TitleDefinitionService titleDefinitionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TitleDefinition>> getAll() {
        return ResponseEntity.ok(titleDefinitionService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TitleDefinition> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(titleDefinitionService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TitleDefinition> save(@RequestBody TitleDefinition titleDefinition) {
        return ResponseEntity.ok(titleDefinitionService.save(titleDefinition));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TitleDefinition> update(@PathVariable Integer id,
                                                  @RequestBody TitleDefinition updated) {
        return ResponseEntity.ok(titleDefinitionService.update(id, updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        titleDefinitionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
