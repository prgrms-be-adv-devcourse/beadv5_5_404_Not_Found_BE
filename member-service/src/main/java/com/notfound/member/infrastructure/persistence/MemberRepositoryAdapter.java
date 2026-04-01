package com.notfound.member.infrastructure.persistence;

import com.notfound.member.application.port.out.MemberRepository;
import com.notfound.member.domain.model.Member;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class MemberRepositoryAdapter implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    public MemberRepositoryAdapter(MemberJpaRepository memberJpaRepository) {
        this.memberJpaRepository = memberJpaRepository;
    }

    @Override
    public Member save(Member member) {
        MemberJpaEntity entity = MemberJpaEntity.from(member);
        MemberJpaEntity saved = memberJpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Member> findById(UUID id) {
        return memberJpaRepository.findById(id).map(MemberJpaEntity::toDomain);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email).map(MemberJpaEntity::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return memberJpaRepository.existsByEmail(email);
    }
}
