package dev.ricardogabriel.miniagendamento.service;


import dev.ricardogabriel.miniagendamento.dto.AgendamentoCreateRequest;
import dev.ricardogabriel.miniagendamento.dto.AgendamentoResponse;
import dev.ricardogabriel.miniagendamento.dto.AgendamentoUpdateRequest;
import dev.ricardogabriel.miniagendamento.mapper.AgendamentoMapper;
import dev.ricardogabriel.miniagendamento.model.Agendamento;
import dev.ricardogabriel.miniagendamento.model.StatusAgendamento;
import dev.ricardogabriel.miniagendamento.repository.AgendamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AgendamentoService {

    private final AgendamentoRepository repo;
    public AgendamentoService(AgendamentoRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public AgendamentoResponse criar(@Valid AgendamentoCreateRequest req) {

        validarIntervalo(req.dataInicio(), req.dataFim());
        checarConflito(req.usuario(), req.dataInicio(), req.dataFim(), null);


        Agendamento entity = AgendamentoMapper.toEntity(req);
        entity = repo.save(entity);
        return AgendamentoMapper.toResponse(entity);
    }

    @Transactional
    public AgendamentoResponse atualizar(Long id, @Valid AgendamentoUpdateRequest req) {
        Agendamento entity = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado!"));
        AgendamentoMapper.merge(entity, req);
        validarIntervalo(req.dataInicio(), req.dataFim());
        checarConflito(entity.getUsuario(), req.dataInicio(), req.dataFim(), entity.getId());

        entity = repo.save(entity);
        return AgendamentoMapper.toResponse(entity);
    }

    @Transactional
    public AgendamentoResponse cancelar(Long id) {
        Agendamento entity = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado!"));
        entity.setStatus(StatusAgendamento.CANCELADO);
        entity = repo.save(entity);
        return AgendamentoMapper.toResponse(entity);

    }

    @Transactional
    public AgendamentoResponse concluir(Long id) {
        Agendamento entity = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado!"));
        entity.setStatus(StatusAgendamento.CONLUIDO);
        entity = repo.save(entity);
        return AgendamentoMapper.toResponse(entity);

    }

    public AgendamentoResponse buscarPorId(Long id) {
        Agendamento a = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encotnrado!"));
        return AgendamentoMapper.toResponse(a);
    }

    private void validarIntervalo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null || !inicio.isBefore(fim)) {
            throw new IllegalArgumentException("Intervalo: dataInicio deve ser anterior a dataFim");
        }
    }

    private void checarConflito(String usuario, LocalDateTime inicio, LocalDateTime fim, Long id) {
        if (repo.existsconflito(usuario, inicio, fim, id)) {
            throw new IllegalArgumentException("Conflito na agenda: Já exisite um agendamento nesse periodo");
        }
    }
}