package com.notfound.member.application.listener;

import com.notfound.member.application.port.out.MemberRepository;
import com.notfound.member.domain.event.SellerApprovedEvent;
import com.notfound.member.domain.model.Member;
import com.notfound.member.domain.model.MemberRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SellerApprovedEventListener {

    private static final Logger log = LoggerFactory.getLogger(SellerApprovedEventListener.class);

    private final MemberRepository memberRepository;

    public SellerApprovedEventListener(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @EventListener
    @Transactional
    public void handle(SellerApprovedEvent event) {
        Member member = memberRepository.findById(event.memberId())
                .orElseThrow(() -> new IllegalStateException(
                        "판매자 승인 이벤트 처리 실패: 회원을 찾을 수 없습니다. memberId=" + event.memberId()));

        member.changeRole(MemberRole.SELLER);
        memberRepository.save(member);
        log.info("판매자 역할 변경 완료: memberId={}, shopName={}", event.memberId(), event.shopName());
    }
}
