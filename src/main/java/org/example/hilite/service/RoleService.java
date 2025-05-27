package org.example.hilite.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.hilite.dto.reqeust.RoleRequestDto;
import org.example.hilite.dto.response.RoleResponseDto;
import org.example.hilite.entity.Role;
import org.example.hilite.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository roleRepository;

  public RoleResponseDto createRole(RoleRequestDto dto) {
    if (roleRepository.findByName(dto.name()).isPresent()) {
      throw new IllegalArgumentException("Role already exists");
    }
    Role role = new Role();
    role.setName(dto.name());
    return toDto(roleRepository.save(role));
  }

  public List<RoleResponseDto> getAllRoles() {
    return roleRepository.findAll().stream().map(this::toDto).toList();
  }

  public void deleteRole(Long id) {
    roleRepository.deleteById(id);
  }

  private RoleResponseDto toDto(Role role) {
    return new RoleResponseDto(role.getId(), role.getName());
  }
}
