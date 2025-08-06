package org.example.hilite.repository;

import org.example.hilite.entity.Member;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MemberRepository extends R2dbcRepository<Member, Long> {

  /**
   * 사용자명으로 멤버 조회
   */
  Mono<Member> findByUsername(String username);

  /**
   * 사용자명 존재 여부 확인
   */
  Mono<Boolean> existsByUsername(String username);

  /**
   * 이메일 존재 여부 확인
   */
  Mono<Boolean> existsByEmail(String email);

  /**
   * 이메일로 멤버 조회
   */
  Mono<Member> findByEmail(String email);
}