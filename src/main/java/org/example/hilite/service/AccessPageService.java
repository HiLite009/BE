package org.example.hilite.service;


import lombok.RequiredArgsConstructor;
import org.example.hilite.entity.AccessPage;
import org.example.hilite.repository.AccessPageRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessPageService {

  private final AccessPageRepository accessPageRepository;

  // 저장하기
  public void saveAccessPage(String path) {
    accessPageRepository.findByPath(path).ifPresent(accessPage -> {
      throw new IllegalArgumentException("이미 존재하는 경로입니다.");
    });

    accessPageRepository.save(new AccessPage(path));
  }
}
