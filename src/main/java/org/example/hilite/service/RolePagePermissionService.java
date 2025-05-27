package org.example.hilite.service;

import lombok.RequiredArgsConstructor;
import org.example.hilite.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RolePagePermissionService {

  private final RoleRepository roleRepository;
}
