package org.example.hilite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.dto.response.ProtectedPageResponseDto;
import org.example.hilite.entity.AccessPage;
import org.example.hilite.repository.AccessPageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessPageService {

  private final AccessPageRepository accessPageRepository;

  /**
   * 접근 페이지 저장
   */
  @Transactional
  public Mono<ProtectedPageResponseDto> saveAccessPage(String path) {
    log.debug("Saving access page: {}", path);

    return accessPageRepository.existsByPath(path)
        .flatMap(exists -> {
          if (exists) {
            return Mono.error(new IllegalArgumentException("이미 존재하는 경로입니다: " + path));
          }

          AccessPage accessPage = new AccessPage(path);
          return accessPageRepository.save(accessPage)
              .map(saved -> new ProtectedPageResponseDto(saved.getId(), saved.getPath()))
              .doOnSuccess(dto ->
                  log.info("Successfully saved access page: {}", dto.path()));
        })
        .doOnError(error ->
            log.error("Failed to save access page {}: {}", path, error.getMessage()));
  }

  /**
   * 모든 접근 페이지 조회
   */
  public Flux<ProtectedPageResponseDto> getAllAccessPages() {
    log.debug("Getting all access pages");

    return accessPageRepository.findAll()
        .map(page -> new ProtectedPageResponseDto(page.getId(), page.getPath()))
        .doOnNext(dto -> log.debug("Retrieved access page: {}", dto.path()))
        .doOnError(error -> log.error("Failed to get all access pages: {}", error.getMessage()));
  }

  /**
   * 접근 페이지 삭제
   */
  @Transactional
  public Mono<Void> deleteAccessPage(Long id) {
    log.debug("Deleting access page with id: {}", id);

    return accessPageRepository.findById(id)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않는 페이지입니다: " + id)))
        .flatMap(accessPage -> {
          log.info("Deleting access page: {}", accessPage.getPath());
          return accessPageRepository.delete(accessPage);
        })
        .doOnSuccess(v -> log.info("Successfully deleted access page with id: {}", id))
        .doOnError(
            error -> log.error("Failed to delete access page {}: {}", id, error.getMessage()));
  }

  /**
   * 경로로 접근 페이지 조회
   */
  public Mono<ProtectedPageResponseDto> getAccessPageByPath(String path) {
    log.debug("Getting access page by path: {}", path);

    return accessPageRepository.findByPath(path)
        .map(page -> new ProtectedPageResponseDto(page.getId(), page.getPath()))
        .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않는 경로입니다: " + path)))
        .doOnSuccess(dto -> log.debug("Found access page: {}", dto.path()))
        .doOnError(
            error -> log.error("Failed to get access page {}: {}", path, error.getMessage()));
  }

  /**
   * 접근 페이지 업데이트
   */
  @Transactional
  public Mono<ProtectedPageResponseDto> updateAccessPage(Long id, String newPath) {
    log.debug("Updating access page {} with new path: {}", id, newPath);

    return accessPageRepository.findById(id)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않는 페이지입니다: " + id)))
        .flatMap(existingPage -> {
          // 다른 페이지가 같은 경로를 사용하는지 확인
          return accessPageRepository.findByPath(newPath)
              .flatMap(pageWithSamePath -> {
                if (!pageWithSamePath.getId().equals(id)) {
                  return Mono.error(
                      new IllegalArgumentException("이미 존재하는 경로입니다: " + newPath));
                }
                return Mono.just(existingPage);
              })
              .switchIfEmpty(Mono.just(existingPage));
        })
        .flatMap(page -> {
          page.setPath(newPath);
          return accessPageRepository.save(page);
        })
        .map(updated -> new ProtectedPageResponseDto(updated.getId(), updated.getPath()))
        .doOnSuccess(dto ->
            log.info("Successfully updated access page {}: {}", id, dto.path()))
        .doOnError(error ->
            log.error("Failed to update access page {}: {}", id, error.getMessage()));
  }
}