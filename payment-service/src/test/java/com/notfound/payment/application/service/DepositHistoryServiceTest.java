package com.notfound.payment.application.service;

import com.notfound.payment.application.port.in.GetDepositHistoryUseCase;
import com.notfound.payment.application.port.out.DepositPort;
import com.notfound.payment.domain.model.DepositType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class DepositHistoryServiceTest {

    @Mock private DepositPort depositPort;

    @InjectMocks
    private DepositHistoryService depositHistoryService;

    @Test
    void getHistory_type_null이면_전체_조회() {
        UUID memberId = UUID.randomUUID();
        given(depositPort.findByMemberId(eq(memberId), any(Pageable.class))).willReturn(Page.empty());

        GetDepositHistoryUseCase.HistoryResult result =
                depositHistoryService.getHistory(memberId, null, 0, 20);

        assertThat(result.content()).isEmpty();
        then(depositPort).should().findByMemberId(eq(memberId), any(Pageable.class));
        then(depositPort).should(never()).findByMemberIdAndType(any(), any(), any());
    }

    @Test
    void getHistory_type_있으면_타입별_조회() {
        UUID memberId = UUID.randomUUID();
        given(depositPort.findByMemberIdAndType(eq(memberId), eq(DepositType.CHARGE), any(Pageable.class)))
                .willReturn(Page.empty());

        depositHistoryService.getHistory(memberId, DepositType.CHARGE, 0, 20);

        then(depositPort).should().findByMemberIdAndType(eq(memberId), eq(DepositType.CHARGE), any(Pageable.class));
        then(depositPort).should(never()).findByMemberId(any(), any());
    }

    @Test
    void getHistory_페이지_정보_정확히_반환() {
        UUID memberId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<com.notfound.payment.domain.model.Deposit> page = new PageImpl<>(List.of(), pageable, 0);
        given(depositPort.findByMemberId(eq(memberId), any(Pageable.class))).willReturn(page);

        GetDepositHistoryUseCase.HistoryResult result =
                depositHistoryService.getHistory(memberId, null, 0, 20);

        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.totalElements()).isEqualTo(0);
        assertThat(result.totalPages()).isEqualTo(0);
    }
}
