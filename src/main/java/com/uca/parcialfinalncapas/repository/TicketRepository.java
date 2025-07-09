package com.uca.parcialfinalncapas.repository;

import com.uca.parcialfinalncapas.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Encuentra todos los tickets cuyo campo usuarioId coincida.
     */
    List<Ticket> findByUsuarioId(Long usuarioId);
}
