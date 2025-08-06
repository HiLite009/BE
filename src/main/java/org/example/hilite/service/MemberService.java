package org.example.hilite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.dto.reqeust.SignupRequestDto;
import org.example.hilite.dto.response.MemberResponseDto;
import org.example.hilite.entity.Member;
import org.example.hilite.entity.MemberRole;
import org.example.hilite.entity.Role;
import org.example.hilite.repository.MemberRepository;
import org.example.hilite.repository.MemberRoleRepository;
import org.example.hilite.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

  private final MemberRepository memberRepository;
  private final MemberRoleRepository memberRoleRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * 회원가입
   */
  public Mono<Void> signup(SignupRequestDto requestDto) {
    log.debug("Attempting signup for username: {}", requestDto.username());

    return validateSignupRequest(requestDto)
        .then(createMemberWithRole(requestDto))
        .doOnSuccess(member -> log.info("Member signup completed: {}", member.getUsername()))
        .doOnError(error -> log.error("Member signup failed: {}", error.getMessage()))
        .then();
  }

  /**
   * 회원가입 유효성 검사
   */
  private Mono<Void> validateSignupRequest(SignupRequestDto requestDto) {
    // 비밀번호 확인 검증
    if (!requestDto.password().equals(requestDto.passwordConfirm())) {
      return Mono.error(new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다."));
    }

    // 사용자명 중복 검사
    return memberRepository.existsByUsername(requestDto.username())
        .flatMap(exists -> {
          if (exists) {
            return Mono.error(new IllegalArgumentException("이미 존재하는 아이디입니다."));
          }
          return Mono.empty();
        })
        // 이메일 중복 검사
        .then(memberRepository.existsByEmail(requestDto.email())
            .flatMap(exists -> {
              if (exists) {
                return Mono.error(new IllegalArgumentException("이미 존재하는 이메일입니다."));
              }
              return Mono.empty();
            }));
  }

  /**
   * 멤버 생성 및 기본 역할 할당
   */
  private Mono<Member> createMemberWithRole(SignupRequestDto requestDto) {
    // 멤버 생성
    Member member = new Member();
    member.setUsername(requestDto.username());
    member.setPassword(passwordEncoder.encode(requestDto.password()));
    member.setEmail(requestDto.email());

    return memberRepository.save(member)
        .flatMap(savedMember ->
            assignDefaultRole(savedMember.getId())
                .thenReturn(savedMember));
  }

  /**
   * 기본 역할(USER) 할당
   */
  private Mono<Void> assignDefaultRole(Long memberId) {
    return roleRepository.findByName("ROLE_USER")
        .switchIfEmpty(Mono.error(new IllegalArgumentException("기본 권한이 설정되어 있지 않습니다.")))
        .flatMap(role -> {
          MemberRole memberRole = new MemberRole(memberId, role.getId());
          return memberRoleRepository.save(memberRole);
        })
        .then();
  }

  /**
   * 이메일 중복 확인
   */
  public Mono<Boolean> checkEmail(String email) {
    return memberRepository.existsByEmail(email);
  }

  /**
   * 사용자 정보 조회 (역할 포함)
   */
  public Mono<MemberResponseDto> getMemberInfo(String username) {
    log.debug("Getting member info for username: {}", username);

    return memberRepository.findByUsername(username)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않는 사용자입니다.")))
        .flatMap(member ->
            getMemberRoles(member.getId())
                .collectList()
                .map(roles -> new MemberResponseDto(
                    member.getId(),
                    member.getUsername(),
                    member.getEmail(),
                    roles
                )))
        .doOnSuccess(memberDto -> log.debug("Retrieved member info: {}", memberDto.username()))
        .doOnError(error -> log.error("Failed to get member info for {}: {}", username, error.getMessage()));
  }

  /**
   * 모든 멤버 조회 (관리자용)
   */
  public Flux<MemberResponseDto> getAllMembers() {
    log.debug("Getting all members");

    return memberRepository.findAll()
        .flatMap(member ->
            getMemberRoles(member.getId())
                .collectList()
                .map(roles -> new MemberResponseDto(
                    member.getId(),
                    member.getUsername(),
                    member.getEmail(),
                    roles
                )))
        .doOnNext(memberDto -> log.debug("Retrieved member: {}", memberDto.username()))
        .doOnError(error -> log.error("Failed to get all members: {}", error.getMessage()));
  }

  /**
   * 특정 멤버의 역할 목록 조회
   */
  private Flux<String> getMemberRoles(Long memberId) {
    return memberRoleRepository.findByMemberId(memberId)
        .flatMap(memberRole ->
            roleRepository.findById(memberRole.getRoleId())
                .map(Role::getName))
        .doOnNext(roleName -> log.debug("Found role for member {}: {}", memberId, roleName));
  }

  /**
   * 멤버에게 역할 추가
   */
  public Mono<Void> addRoleToMember(String username, String roleName) {
    log.debug("Adding role {} to member {}", roleName, username);

    return memberRepository.findByUsername(username)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않는 사용자입니다.")))
        .flatMap(member ->
            roleRepository.findByName(roleName)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않는 역할입니다.")))
                .flatMap(role ->
                    // 이미 해당 역할이 있는지 확인
                    memberRoleRepository.existsByMemberIdAndRoleId(member.getId(), role.getId())
                        .flatMap(exists -> {
                          if (exists) {
                            return Mono.error(new IllegalArgumentException("이미 해당 역할을 가지고 있습니다."));
                          }

                          MemberRole memberRole = new MemberRole(member.getId(), role.getId());
                          return memberRoleRepository.save(memberRole);
                        })))
        .doOnSuccess(memberRole -> log.info("Successfully added role {} to member {}", roleName, username))
        .then();
  }

  /**
   * 멤버에서 역할 제거
   */
  public Mono<Void> removeRoleFromMember(String username, String roleName) {
    log.debug("Removing role {} from member {}", roleName, username);

    return memberRepository.findByUsername(username)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않는 사용자입니다.")))
        .flatMap(member ->
            roleRepository.findByName(roleName)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않는 역할입니다.")))
                .flatMap(role ->
                    memberRoleRepository.deleteByMemberIdAndRoleId(member.getId(), role.getId())))
        .doOnSuccess(v -> log.info("Successfully removed role {} from member {}", roleName, username));
  }

  /**
   * 사용자명으로 멤버와 역할 정보 조회 (UserDetails용)
   */
  public Mono<Member> findMemberWithRoles(String username) {
    return memberRepository.findByUsername(username)
        .doOnNext(member -> log.debug("Found member for authentication: {}", member.getUsername()));
  }
}