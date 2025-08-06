package org.example.hilite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.dto.reqeust.RoleRequestDto;
import org.example.hilite.dto.response.RoleResponseDto;
import org.example.hilite.entity.Role;
import org.example.hilite.repository.RoleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

  private final RoleRepository roleRepository;

  /**
   * 역할 생성
   */
  public Mono<RoleResponseDto> createRole(RoleRequestDto dto) {
    log.debug("Creating role: {}", dto.name());

    return roleRepository.existsByName(dto.name())
        .flatMap(exists -> {
          if (exists) {
            return Mono.error(new IllegalArgumentException("이미 존재하는 역할입니다: " + dto.name()));
          }

          Role role = new Role();
          role.setName(dto.name());

          return roleRepository.save(role)
              .map(this::toDto)
              .doOnSuccess(savedRole ->
                  log.info("Successfully created role: {}", savedRole.name()));
        })
        .doOnError(error ->
            log.error("Failed to create role {}: {}", dto.name(), error.getMessage()));
  }

  /**
   * 모든 역할 조회
   */
  public Flux<RoleResponseDto> getAllRoles() {
    log.debug("Getting all roles");

    return roleRepository.findAll()
        .map(this::toDto)
        .doOnNext(role -> log.debug("Retrieved role: {}", role.name()))
        .doOnError(error -> log.error("Failed to get all roles: {}", error.getMessage()));
  }

  /**
   * 역할 삭제
   */
  public Mono<Void> deleteRole(Long id) {
    log.debug("Deleting role with id: {}", id);

    return roleRepository.findById(id)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않는 역할입니다: " + id)))
        .flatMap(role -> {
          log.info("Deleting role: {}", role.getName());
          return roleRepository.delete(role);
        })
        .doOnSuccess(v -> log.info("Successfully deleted role with id: {}", id))
        .doOnError(error -> log.error("Failed to delete role {}: {}", id, error.getMessage()));
  }

  /**
   * 역할명으로 역할 조회
   */
  public Mono<RoleResponseDto> getRoleByName(String name) {
    log.debug("Getting role by name: {}", name);

    return roleRepository.findByName(name)
        .map(this::toDto)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않는 역할입니다: " + name)))
        .doOnSuccess(role -> log.debug("Found role: {}", role.name()))
        .doOnError(error -> log.error("Failed to get role {}: {}", name, error.getMessage()));
  }

  /**
   * 역할 업데이트
   */
  public Mono<RoleResponseDto> updateRole(Long id, RoleRequestDto dto) {
    log.debug("Updating role {} with new name: {}", id, dto.name());

    return roleRepository.findById(id)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않는 역할입니다: " + id)))
        .flatMap(existingRole -> {
          // 다른 역할이 같은 이름을 사용하는지 확인
          return roleRepository.findByName(dto.name())
              .flatMap(roleWithSameName -> {
                if (!roleWithSameName.getId().equals(id)) {
                  return Mono.error(new IllegalArgumentException("이미 존재하는 역할명입니다: " + dto.name()));
                }
                return Mono.just(existingRole);
              })
              .switchIfEmpty(Mono.just(existingRole));
        })
        .flatMap(role -> {
          role.setName(dto.name());
          return roleRepository.save(role);
        })
        .map(this::toDto)
        .doOnSuccess(updatedRole ->
            log.info("Successfully updated role {}: {}", id, updatedRole.name()))
        .doOnError(error ->
            log.error("Failed to update role {}: {}", id, error.getMessage()));
  }

  /**
   * Entity를 DTO로 변환
   */
  private RoleResponseDto toDto(Role role) {
    return new RoleResponseDto(role.getId(), role.getName());
  }
}