package com.notfound.settlement.application.service;

import com.notfound.settlement.application.port.in.CreateSettlementTargetCommand;
import com.notfound.settlement.application.port.out.ProcessedEventRepository;
import com.notfound.settlement.application.port.out.SettlementTargetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SettlementTargetServiceTest {

    @Mock
    private SettlementTargetRepository settlementTargetRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private SettlementTargetService settlementTargetService;

    @Nested
    @DisplayName("createSettlementTarget()")
    class CreateSettlementTarget {

        @Test
        @DisplayName("이미 처리된 이벤트는 저장 없이 무시한다")
        void duplicateEvent_ignored() {
            String eventId = UUID.randomUUID().toString();
            CreateSettlementTargetCommand command = new CreateSettlementTargetCommand(
                    eventId, UUID.randomUUID(), UUID.randomUUID(), 10000L, LocalDateTime.now());
            given(processedEventRepository.existsById(eventId)).willReturn(true);

            settlementTargetService.createSettlementTarget(command);

            verify(settlementTargetRepository, never()).save(any());
            verify(processedEventRepository, never()).save(any());
        }

        @Test
        @DisplayName("새로운 이벤트는 정산 대상을 저장하고 처리 이력을 기록한다")
        void newEvent_saved() {
            String eventId = UUID.randomUUID().toString();
            CreateSettlementTargetCommand command = new CreateSettlementTargetCommand(
                    eventId, UUID.randomUUID(), UUID.randomUUID(), 10000L, LocalDateTime.now());
            given(processedEventRepository.existsById(eventId)).willReturn(false);

            settlementTargetService.createSettlementTarget(command);

            verify(settlementTargetRepository).save(any());
            verify(processedEventRepository).save(eventId);
        }
    }
}
