package com.uca.parcialfinalncapas.service;

import com.uca.parcialfinalncapas.dto.request.TicketCreateRequest;
import com.uca.parcialfinalncapas.dto.request.TicketUpdateRequest;
import com.uca.parcialfinalncapas.dto.response.TicketResponse;

import java.util.List;

public interface TicketService {

    /**
     * Crea un ticket genérico (usa los correos en el request).
     */
    TicketResponse createTicket(TicketCreateRequest ticket);

    /**
     * Crea un ticket asociado al usuario autenticado.
     */
    TicketResponse createTicketForUser(TicketCreateRequest ticket, String correoUsuario);

    /**
     * Actualiza un ticket completo (incluye cambio de técnico).
     */
    TicketResponse updateTicket(TicketUpdateRequest ticket);

    /**
     * Elimina un ticket por su ID.
     */
    void deleteTicket(Long id);

    /**
     * Obtiene un ticket por su ID.
     */
    TicketResponse getTicketById(Long id);

    /**
     * Obtiene todos los tickets (para ROLE_TECH).
     */
    List<TicketResponse> getAllTickets();

    /**
     * Obtiene solo los tickets del usuario dado (para ROLE_USER).
     */
    List<TicketResponse> getTicketsByUser(String correoUsuario);
}
