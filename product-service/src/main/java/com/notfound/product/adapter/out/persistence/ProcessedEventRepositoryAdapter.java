package com.notfound.product.adapter.out.persistence;

import com.notfound.product.application.port.out.ProcessedEventRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProcessedEventRepositoryAdapter implements ProcessedEventRepository {

    private final ProcessedEventJpaRepository processedEventJpaRepository;

    public ProcessedEventRepositoryAdapter(ProcessedEventJpaRepository processedEventJpaRepository) {
        this.processedEventJpaRepository = processedEventJpaRepository;
    }

    @Override
    public boolean existsById(String eventId) {
        return processedEventJpaRepository.existsById(eventId);
    }

    @Override
    public void save(String eventId) {
        processedEventJpaRepository.save(new ProcessedEventJpaEntity(eventId, LocalDateTime.now()));
    }
}
