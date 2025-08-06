package org.example.hilite.repository;

import org.example.hilite.entity.MemberRole;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MemberRoleRepository extends R2dbcRepository<MemberRole, Long> {

  /**
   * 특정 멤버의 모든 역할 매핑 조회
   */
  Flux<MemberRole> findByMemberId(Long memberId);

  /**
   * 특정 역할의 모든 멤버 매핑 조회
   */
  Flux<MemberRole> findByRoleId(Long roleId);

  /**
   * 특정 멤버-역할 매핑 조회
   */
  Mono<MemberRole> findByMemberIdAndRoleId(Long memberId, Long roleId);

  /**
   * 특정 멤버-역할 매핑 존재 여부 확인
   */
  Mono<Boolean> existsByMemberIdAndRoleId(Long memberId, Long roleId);

  /**
   * 특정 멤버의 모든 역할 매핑 삭제
   */
  @Query("DELETE FROM member_role WHERE member_id = :memberId")
  Mono<Void> deleteByMemberId(@Param("memberId") Long memberId);

  /**
   * 특정 역할의 모든 멤버 매핑 삭제
   */
  @Query("DELETE FROM member_role WHERE role_id = :roleId")
  Mono<Void> deleteByRoleId(@Param("roleId") Long roleId);

  /**
   * 특정 멤버-역할 매핑 삭제
   */
  @Query("DELETE FROM member_role WHERE member_id = :memberId AND role_id = :roleId")
  Mono<Void> deleteByMemberIdAndRoleId(@Param("memberId") Long memberId, @Param("roleId") Long roleId);
}