package org.example.hilite.dto.reqeust;

public record RolePagePermissionRequestDto(Long roleId, Long pageId, boolean canAccess) {}
