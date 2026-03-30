package com.notfound.settlement.application.service;

import com.notfound.settlement.application.port.in.CreateSettlementTargetCommand;
import com.notfound.settlement.application.port.in.CreateSettlementTargetUseCase;
import com.notfound.settlement.application.port.out.ProcessedEventRepository;
import com.notfound.settlement.application.port.out.SettlementTargetRepository;
import com.notfound.settlement.domain.model.SettlementTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementTargetService implements CreateSettlementTargetUseCase {

    private final SettlementTargetRepository settlementTargetRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    @Override
    public void createSettlementTarget(CreateSettlementTargetCommand command) {
        // TODO: KafkaListener concurrency > 1 또는 멀티 파티션 운영 시 existsById→save 패턴이
        //       비원자적으로 동작하여 정산 대상 중복 생성이 발생할 수 있음.
        //       해당 시점에 INSERT ON CONFLICT DO NOTHING 기반 원자적 처리로 교체 필요.
        if (processedEventRepository.existsById(command.eventId())) {
            return;
        }

        SettlementTarget target = SettlementTarget.create(
                command.orderId(),
                command.sellerId(),
                command.totalAmount(),
                command.confirmedAt()
        );

        settlementTargetRepository.save(target);
        processedEventRepository.save(command.eventId());
    }
}
