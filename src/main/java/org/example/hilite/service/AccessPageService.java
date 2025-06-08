package org.example.hilite.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.hilite.dto.response.ProtectedPageResponseDto;
import org.example.hilite.entity.AccessPage;
import org.example.hilite.repository.AccessPageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccessPageService {

  private final AccessPageRepository accessPageRepository;

  @Transactional
  public ProtectedPageResponseDto saveAccessPage(String path) {
    accessPageRepository
        .findByPath(path)
        .ifPresent(
            accessPage -> {
              throw new IllegalArgumentException("이미 존재하는 경로입니다.");
            });

    AccessPage saved = accessPageRepository.save(new AccessPage(path));
    return new ProtectedPageResponseDto(saved.getId(), saved.getPath());
  }

  @Transactional(readOnly = true)
  public List<ProtectedPageResponseDto> getAllAccessPages() {
    return accessPageRepository.findAll().stream()
        .map(page -> new ProtectedPageResponseDto(page.getId(), page.getPath()))
        .toList();
  }

  @Transactional
  public void deleteAccessPage(Long id) {
    AccessPage accessPage =
        accessPageRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 페이지입니다."));

    accessPageRepository.delete(accessPage);
  }
}
