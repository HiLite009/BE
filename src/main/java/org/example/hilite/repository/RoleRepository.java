package org.example.hilite.repository;

import org.example.hilite.entity.Role;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RoleRepository extends R2dbcRepository<Role, Long> {

  /**
   * 역할명으로 역할 조회
   */
  Mono<Role> findByName(String name);

  /**
   * 특정 멤버의 모든 역할 조회
   */
  @Query("""
      SELECT r.* FROM role r
      INNER JOIN member_role mr ON r.id = mr.role_id
      WHERE mr.member_id = :memberId
      """)
  Flux<Role> findRolesByMemberId(@Param("memberId") Long memberId);

  /**
   * 역할명이 존재하는지 확인
   */
  Mono<Boolean> existsByName(String name);
}