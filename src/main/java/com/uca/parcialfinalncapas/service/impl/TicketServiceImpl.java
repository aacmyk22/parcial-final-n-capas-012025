package com.uca.parcialfinalncapas.service.impl;

import com.uca.parcialfinalncapas.dto.request.TicketCreateRequest;
import com.uca.parcialfinalncapas.dto.request.TicketUpdateRequest;
import com.uca.parcialfinalncapas.dto.response.TicketResponse;
import com.uca.parcialfinalncapas.entities.Ticket;
import com.uca.parcialfinalncapas.entities.User;
import com.uca.parcialfinalncapas.exceptions.BadTicketRequestException;
import com.uca.parcialfinalncapas.exceptions.TicketNotFoundException;
import com.uca.parcialfinalncapas.exceptions.UserNotFoundException;
import com.uca.parcialfinalncapas.repository.TicketRepository;
import com.uca.parcialfinalncapas.repository.UserRepository;
import com.uca.parcialfinalncapas.service.TicketService;
import com.uca.parcialfinalncapas.utils.enums.Rol;
import com.uca.parcialfinalncapas.utils.mappers.TicketMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TicketResponse createTicket(TicketCreateRequest ticket) {
        User usuarioSolicitante = userRepository.findByCorreo(ticket.getCorreoUsuario())
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuario no encontrado con correo: " + ticket.getCorreoUsuario()));

        User usuarioSoporte = userRepository.findByCorreo(ticket.getCorreoSoporte())
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuario asignado no encontrado con correo: " + ticket.getCorreoSoporte()));

        if (!Rol.TECH.getValue().equals(usuarioSoporte.getNombreRol())) {
            throw new BadTicketRequestException("El usuario asignado no es un técnico de soporte");
        }

        Ticket saved = ticketRepository.save(
                TicketMapper.toEntityCreate(
                        ticket,
                        usuarioSolicitante.getId(),
                        usuarioSoporte.getId()
                )
        );

        return TicketMapper.toDTO(
                saved,
                usuarioSolicitante.getCorreo(),
                usuarioSoporte.getCorreo()
        );
    }

    @Override
    @Transactional
    public TicketResponse createTicketForUser(TicketCreateRequest ticket, String correoUsuario) {
        ticket.setCorreoUsuario(correoUsuario);
        return createTicket(ticket);
    }

    @Override
    @Transactional
    public TicketResponse updateTicket(TicketUpdateRequest ticket) {
        Ticket existing = ticketRepository.findById(ticket.getId())
                .orElseThrow(() -> new TicketNotFoundException(
                        "Ticket no encontrado con ID: " + ticket.getId()));

        User usuarioSolicitante = userRepository.findById(existing.getUsuarioId())
                .orElseThrow(() -> new UserNotFoundException("Usuario solicitante no encontrado"));

        User usuarioSoporte = userRepository.findByCorreo(ticket.getCorreoSoporte())
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuario asignado no encontrado con correo: " + ticket.getCorreoSoporte()));

        if (!Rol.TECH.getValue().equals(usuarioSoporte.getNombreRol())) {
            throw new BadTicketRequestException("El usuario asignado no es un técnico de soporte");
        }

        Ticket saved = ticketRepository.save(
                TicketMapper.toEntityUpdate(ticket, usuarioSoporte.getId(), existing)
        );

        return TicketMapper.toDTO(
                saved,
                usuarioSolicitante.getCorreo(),
                usuarioSoporte.getCorreo()
        );
    }

    @Override
    public void deleteTicket(Long id) {
        Ticket existing = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(
                        "Ticket no encontrado con ID: " + id));
        ticketRepository.delete(existing);
    }

    @Override
    public TicketResponse getTicketById(Long id) {
        Ticket existing = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(
                        "Ticket no encontrado con ID: " + id));

        User usuarioSolicitante = userRepository.findById(existing.getUsuarioId())
                .orElseThrow(() -> new UserNotFoundException("Usuario solicitante no encontrado"));

        User usuarioSoporte = userRepository.findById(existing.getTecnicoAsignadoId())
                .orElseThrow(() -> new UserNotFoundException("Usuario asignado no encontrado"));

        return TicketMapper.toDTO(
                existing,
                usuarioSolicitante.getCorreo(),
                usuarioSoporte.getCorreo()
        );
    }

    @Override
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(t -> {
                    User solicitante = userRepository.findById(t.getUsuarioId())
                            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
                    User soporte = userRepository.findById(t.getTecnicoAsignadoId())
                            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
                    return TicketMapper.toDTO(
                            t,
                            solicitante.getCorreo(),
                            soporte.getCorreo()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketResponse> getTicketsByUser(String correoUsuario) {
        // 1) Obtener el usuario y su ID
        User usuario = userRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuario no encontrado con correo: " + correoUsuario));
        Long usuarioId = usuario.getId();

        // 2) Buscar tickets por usuarioId
        return ticketRepository.findByUsuarioId(usuarioId).stream()
                .map(t -> {
                    User solicitante = usuario;
                    User soporte = userRepository.findById(t.getTecnicoAsignadoId())
                            .orElseThrow(() -> new UserNotFoundException(
                                    "Usuario soporte no encontrado con ID: " + t.getTecnicoAsignadoId()));
                    return TicketMapper.toDTO(
                            t,
                            solicitante.getCorreo(),
                            soporte.getCorreo()
                    );
                })
                .collect(Collectors.toList());
    }
}
