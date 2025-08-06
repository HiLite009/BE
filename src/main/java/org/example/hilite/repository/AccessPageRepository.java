package org.example.hilite.repository;

import org.example.hilite.entity.AccessPage;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface AccessPageRepository extends R2dbcRepository<AccessPage, Long> {

  /**
   * 경로로 접근 페이지 조회
   */
  Mono<AccessPage> findByPath(String path);

  /**
   * 경로 존재 여부 확인
   */
  Mono<Boolean> existsByPath(String path);
}