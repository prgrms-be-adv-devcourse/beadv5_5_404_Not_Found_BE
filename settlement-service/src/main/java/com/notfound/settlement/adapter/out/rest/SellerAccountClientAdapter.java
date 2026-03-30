package com.notfound.settlement.adapter.out.rest;

import com.notfound.settlement.adapter.out.rest.dto.MemberApiResponse;
import com.notfound.settlement.adapter.out.rest.dto.SellerAccountData;
import com.notfound.settlement.application.port.out.SellerAccount;
import com.notfound.settlement.application.port.out.SellerAccountClient;
import com.notfound.settlement.domain.exception.SellerAccountNotFoundException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerAccountClientAdapter implements SellerAccountClient {

    private final MemberFeignClient memberFeignClient;

    @Override
    public SellerAccount findSellerAccount(UUID sellerId) {
        try {
            MemberApiResponse<SellerAccountData> response = memberFeignClient.getSellerAccount(sellerId);
            if (response.data() == null) {
                throw new SellerAccountNotFoundException(sellerId);
            }
            return new SellerAccount(
                    response.data().bankCode(),
                    response.data().bankAccount(),
                    response.data().accountHolder()
            );
        } catch (SellerAccountNotFoundException e) {
            throw e;
        } catch (FeignException e) {
            log.warn("[SellerAccountClient] member-service 호출 실패 sellerId={} status={}", sellerId, e.status());
            throw new SellerAccountNotFoundException(sellerId);
        }
    }
}
