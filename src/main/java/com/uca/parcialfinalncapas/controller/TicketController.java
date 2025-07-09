package com.uca.parcialfinalncapas.controller;

import com.uca.parcialfinalncapas.dto.request.TicketCreateRequest;
import com.uca.parcialfinalncapas.dto.request.TicketUpdateRequest;
import com.uca.parcialfinalncapas.dto.response.GeneralResponse;
import com.uca.parcialfinalncapas.dto.response.TicketResponse;
import com.uca.parcialfinalncapas.exceptions.BadTicketRequestException;
import com.uca.parcialfinalncapas.service.TicketService;
import com.uca.parcialfinalncapas.utils.ResponseBuilderUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@AllArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    /**
     * GET /api/tickets
     * - TECH ve todos los tickets
     * - USER ve solo sus tickets
     */
    @GetMapping
    public ResponseEntity<GeneralResponse> getAllTickets(Authentication auth) {
        boolean isTech = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TECH"));

        List<TicketResponse> data = isTech
                ? ticketService.getAllTickets()
                : ticketService.getTicketsByUser(auth.getName());

        HttpStatus status = data.isEmpty() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseBuilderUtil.buildResponse(
                "Tickets obtenidos correctamente", status, data
        );
    }

    /**
     * GET /api/tickets/{id}
     * - TECH puede ver cualquier ticket
     * - USER solo su propio ticket
     */
    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse> getTicketById(
            @PathVariable Long id,
            Authentication auth
    ) {
        TicketResponse ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            throw new BadTicketRequestException("Ticket no encontrado");
        }

        boolean isTech = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TECH"));

        // Usamos getCorreoSolicitante(), que es el nombre en tu DTO
        if (!isTech && !ticket.getCorreoSolicitante().equals(auth.getName())) {
            return ResponseBuilderUtil.buildResponse(
                    "Acceso denegado", HttpStatus.FORBIDDEN, null
            );
        }

        return ResponseBuilderUtil.buildResponse(
                "Ticket encontrado", HttpStatus.OK, ticket
        );
    }

    /**
     * POST /api/tickets
     * - SOLO USER puede crear tickets
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GeneralResponse> createTicket(
            Authentication auth,
            @Valid @RequestBody TicketCreateRequest req
    ) {
        TicketResponse created =
                ticketService.createTicketForUser(req, auth.getName());
        return ResponseBuilderUtil.buildResponse(
                "Ticket creado correctamente", HttpStatus.CREATED, created
        );
    }

    /**
     * PUT /api/tickets/{id}
     * - SOLO TECH puede actualizar estado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TECH')")
    public ResponseEntity<GeneralResponse> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketUpdateRequest req
    ) {
        req.setId(id);
        TicketResponse updated = ticketService.updateTicket(req);
        return ResponseBuilderUtil.buildResponse(
                "Ticket actualizado correctamente", HttpStatus.OK, updated
        );
    }

    /**
     * DELETE /api/tickets/{id}
     * - SOLO TECH puede eliminar tickets
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TECH')")
    public ResponseEntity<GeneralResponse> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseBuilderUtil.buildResponse(
                "Ticket eliminado correctamente", HttpStatus.OK, null
        );
    }
}
