package com.notfound.order.infrastructure.client;

import com.notfound.order.application.port.out.MemberServicePort;
import com.notfound.order.domain.exception.OrderException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class MemberServiceClient implements MemberServicePort {

    private final RestClient restClient;

    public MemberServiceClient(@Value("${service.member.url:http://localhost:8081}") String memberServiceUrl,
                               @Value("${internal.secret-key}") String internalSecretKey) {
        this.restClient = RestClient.builder()
                .baseUrl(memberServiceUrl)
                .defaultHeader("X-Internal-Secret", internalSecretKey)
                .build();
    }

    @Override
    public boolean isActiveMember(UUID memberId) {
        var response = restClient.get()
                .uri("/internal/member/{memberId}/active", memberId)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        if (response == null || response.get("data") == null) return false;
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        return Boolean.TRUE.equals(data.get("active"));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getAddresses(UUID memberId) {
        var response = restClient.get()
                .uri("/internal/member/{memberId}/addresses", memberId)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        if (response == null || response.get("data") == null) return List.of();
        return (List<Map<String, Object>>) response.get("data");
    }

    @Override
    @SuppressWarnings("unchecked")
    public int getDepositBalance(UUID memberId) {
        var response = restClient.get()
                .uri("/internal/member/{memberId}/deposit", memberId)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        if (response == null || response.get("data") == null) return 0;
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        return ((Number) data.get("depositBalance")).intValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public int deductDeposit(UUID memberId, int amount) {
        try {
            var response = restClient.post()
                    .uri("/internal/member/{memberId}/deposit/deduct", memberId)
                    .body(Map.of("amount", amount))
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
            if (response == null || response.get("data") == null) return 0;
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            return ((Number) data.get("remainingBalance")).intValue();
        } catch (Exception e) {
            throw OrderException.insufficientDeposit();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public int chargeDeposit(UUID memberId, int amount) {
        var response = restClient.post()
                .uri("/internal/member/{memberId}/deposit/charge", memberId)
                .body(Map.of("amount", amount))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        if (response == null || response.get("data") == null) return 0;
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        return ((Number) data.get("remainingBalance")).intValue();
    }
}
