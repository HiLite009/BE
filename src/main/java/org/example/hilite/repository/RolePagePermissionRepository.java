package org.example.hilite.repository;

import java.util.List;
import org.example.hilite.entity.RolePagePermission;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RolePagePermissionRepository extends R2dbcRepository<RolePagePermission, Long> {

  /**
   * 특정 역할명들과 경로로 권한 조회 (동적 권한 체크용)
   */
  @Query("""
      SELECT rpp.* FROM role_page_permission rpp
      INNER JOIN role r ON rpp.role_id = r.id
      INNER JOIN access_page ap ON rpp.access_page_id = ap.id
      WHERE r.name IN (:roleNames) AND ap.path = :path
      """)
  Flux<RolePagePermission> findByRoleNamesAndPath(
      @Param("roleNames") List<String> roleNames,
      @Param("path") String path);

  /**
   * 특정 역할명들의 모든 권한 조회 (패턴 매칭용)
   */
  @Query("""
      SELECT rpp.* FROM role_page_permission rpp
      INNER JOIN role r ON rpp.role_id = r.id
      WHERE r.name IN (:roleNames)
      """)
  Flux<RolePagePermission> findByRoleNames(@Param("roleNames") List<String> roleNames);

  /**
   * 역할 ID와 페이지 ID로 권한 조회
   */
  Mono<RolePagePermission> findByRoleIdAndAccessPageId(Long roleId, Long accessPageId);

  /**
   * 역할 ID와 페이지 ID로 권한 존재 여부 확인
   */
  Mono<Boolean> existsByRoleIdAndAccessPageId(Long roleId, Long accessPageId);

  /**
   * 특정 역할의 모든 권한 조회
   */
  Flux<RolePagePermission> findByRoleId(Long roleId);

  /**
   * 특정 페이지의 모든 권한 조회
   */
  Flux<RolePagePermission> findByAccessPageId(Long accessPageId);

  /**
   * 특정 역할의 모든 권한 삭제
   */
  @Query("DELETE FROM role_page_permission WHERE role_id = :roleId")
  Mono<Void> deleteByRoleId(@Param("roleId") Long roleId);

  /**
   * 특정 페이지의 모든 권한 삭제
   */
  @Query("DELETE FROM role_page_permission WHERE access_page_id = :accessPageId")
  Mono<Void> deleteByAccessPageId(@Param("accessPageId") Long accessPageId);

  /**
   * 동적 권한 체크를 위한 최적화된 쿼리
   * 역할명들과 경로 패턴으로 권한이 있는지 확인
   */
  @Query("""
      SELECT COUNT(rpp.id) > 0 FROM role_page_permission rpp
      INNER JOIN role r ON rpp.role_id = r.id
      INNER JOIN access_page ap ON rpp.access_page_id = ap.id
      WHERE r.name IN (:roleNames) 
      AND (ap.path = :exactPath OR ap.path LIKE :pathPattern)
      """)
  Mono<Boolean> hasPermissionForPath(
      @Param("roleNames") List<String> roleNames,
      @Param("exactPath") String exactPath,
      @Param("pathPattern") String pathPattern);
}