package org.example.hilite.dto.response;

public record RolePagePermissionResponseDto(
    Long id, Long roleId, String roleName, Long pageId, String path, boolean canAccess) {}
