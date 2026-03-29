package com.notfound.settlement.adapter.out.persistence;

import com.notfound.settlement.application.port.out.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ProcessedEventRepositoryAdapter implements ProcessedEventRepository {

    private final ProcessedEventJpaRepository processedEventJpaRepository;

    @Override
    public boolean existsById(String eventId) {
        return processedEventJpaRepository.existsById(eventId);
    }

    @Override
    public void save(String eventId) {
        processedEventJpaRepository.save(new ProcessedEventJpaEntity(eventId, LocalDateTime.now()));
    }
}
