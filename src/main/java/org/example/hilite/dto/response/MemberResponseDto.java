package org.example.hilite.dto.response;

import java.util.List;

public record MemberResponseDto(Long id, String username, String email, List<String> roles
    //    LocalDateTime createdDate,
    //    LocalDateTime lastModifiedDate
    ) {}
