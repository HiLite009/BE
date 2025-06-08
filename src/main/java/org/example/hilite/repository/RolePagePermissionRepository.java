package org.example.hilite.repository;

import java.util.List;
import java.util.Optional;
import org.example.hilite.entity.RolePagePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RolePagePermissionRepository extends JpaRepository<RolePagePermission, Long> {

  @Query("SELECT rpp FROM RolePagePermission rpp " +
      "JOIN rpp.role r " +
      "JOIN rpp.accessPage ap " +
      "WHERE r.name IN :roleNames AND ap.path = :path")
  List<RolePagePermission> findByRoleNamesAndPath(@Param("roleNames") List<String> roleNames,
      @Param("path") String path);

  @Query("SELECT rpp FROM RolePagePermission rpp " +
      "JOIN rpp.role r " +
      "JOIN rpp.accessPage ap " +
      "WHERE r.name IN :roleNames")
  List<RolePagePermission> findByRoleNames(@Param("roleNames") List<String> roleNames);

  @Query("SELECT rpp FROM RolePagePermission rpp " +
      "WHERE rpp.role.id = :roleId AND rpp.accessPage.id = :pageId")
  Optional<RolePagePermission> findByRoleIdAndPageId(@Param("roleId") Long roleId,
      @Param("pageId") Long pageId);

  @Query("SELECT rpp FROM RolePagePermission rpp " +
      "JOIN FETCH rpp.role r " +
      "JOIN FETCH rpp.accessPage ap " +
      "WHERE r.name = :roleName")
  List<RolePagePermission> findByRoleNameWithDetails(@Param("roleName") String roleName);
}
