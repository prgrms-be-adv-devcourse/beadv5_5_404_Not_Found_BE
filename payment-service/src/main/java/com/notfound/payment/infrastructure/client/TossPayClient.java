package com.notfound.payment.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.notfound.payment.application.port.out.PgPort;
import com.notfound.payment.domain.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
public class TossPayClient implements PgPort {

    private static final String TOSS_CANCEL_URL_TEMPLATE = "https://api.tosspayments.com/v1/payments/%s/cancel";

    private final RestClient restClient;
    private final TossProperties tossProperties;

    public TossPayClient(TossProperties tossProperties) {
        this.tossProperties = tossProperties;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    @Override
    public PgConfig getConfig() {
        return new PgConfig(
                tossProperties.clientKey(),
                tossProperties.successUrl(),
                tossProperties.failUrl()
        );
    }

    @Override
    public PgConfirmResult confirm(PgConfirmCommand command) {
        String authorization = buildBasicAuth(tossProperties.secretKey());

        try {
            TossConfirmResponse response = restClient.post()
                    .uri(tossProperties.confirmUrl())
                    .header("Authorization", authorization)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "paymentKey", command.paymentKey(),
                            "orderId", command.orderId(),
                            "amount", command.amount()
                    ))
                    .retrieve()
                    .onStatus(status -> status.isError(), (req, res) -> {
                        log.error("Toss 결제 승인 실패: status={}", res.getStatusCode());
                        throw PaymentException.pgConfirmFailed();
                    })
                    .body(TossConfirmResponse.class);

            assert response != null;
            return new PgConfirmResult(
                    response.transactionKey(),
                    response.paymentKey(),
                    parseDateTime(response.approvedAt()),
                    response.method()
            );
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Toss 결제 승인 중 예외 발생", e);
            throw PaymentException.pgConfirmFailed(e);
        }
    }

    @Override
    public PgCancelResult cancel(PgCancelCommand command) {
        String authorization = buildBasicAuth(tossProperties.secretKey());
        String url = String.format(TOSS_CANCEL_URL_TEMPLATE, command.paymentKey());

        try {
            TossCancelResponse response = restClient.post()
                    .uri(url)
                    .header("Authorization", authorization)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "cancelReason", command.cancelReason(),
                            "cancelAmount", command.cancelAmount()
                    ))
                    .retrieve()
                    .onStatus(status -> status.isError(), (req, res) -> {
                        log.error("Toss 결제 취소 실패: status={}", res.getStatusCode());
                        throw PaymentException.pgCancelFailed();
                    })
                    .body(TossCancelResponse.class);

            if (response == null || response.cancels() == null || response.cancels().isEmpty()) {
                throw PaymentException.pgCancelFailed();
            }
            TossCancelResponse.CancelDetail lastCancel = response.cancels().get(response.cancels().size() - 1);
            return new PgCancelResult(
                    lastCancel.transactionKey(),
                    parseDateTime(lastCancel.canceledAt())
            );
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Toss 결제 취소 중 예외 발생", e);
            throw PaymentException.pgCancelFailed(e);
        }
    }

    private String buildBasicAuth(String secretKey) {
        String credentials = secretKey + ":";
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private LocalDateTime parseDateTime(String isoDateTime) {
        return OffsetDateTime.parse(isoDateTime).toLocalDateTime();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TossConfirmResponse(
            String paymentKey,
            String orderId,
            String transactionKey,
            String status,
            String approvedAt,
            String method,
            int totalAmount
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TossCancelResponse(
            String paymentKey,
            List<CancelDetail> cancels
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record CancelDetail(
                String transactionKey,
                String canceledAt,
                int cancelAmount
        ) {}
    }
}
