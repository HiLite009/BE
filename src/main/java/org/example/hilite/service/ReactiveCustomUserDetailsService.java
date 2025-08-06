package org.example.hilite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.entity.Member;
import org.example.hilite.repository.MemberRepository;
import org.example.hilite.repository.MemberRoleRepository;
import org.example.hilite.repository.RoleRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactiveCustomUserDetailsService implements ReactiveUserDetailsService {

  private final MemberRepository memberRepository;
  private final MemberRoleRepository memberRoleRepository;
  private final RoleRepository roleRepository;

  @Override
  public Mono<UserDetails> findByUsername(String username) {
    log.debug("Loading user by username: {}", username);

    return memberRepository.findByUsername(username)
        .switchIfEmpty(Mono.error(new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username)))
        .flatMap(member ->
            loadMemberRoles(member)
                .map(memberWithRoles -> createUserDetails(memberWithRoles)))
        .doOnSuccess(userDetails ->
            log.debug("Successfully loaded user: {} with authorities: {}",
                userDetails.getUsername(),
                userDetails.getAuthorities()))
        .doOnError(error ->
            log.error("Failed to load user {}: {}", username, error.getMessage()));
  }

  /**
   * 멤버의 역할 정보를 로드하여 CustomUserDetails 생성을 위한 Member 객체 준비
   */
  private Mono<Member> loadMemberRoles(Member member) {
    return memberRoleRepository.findByMemberId(member.getId())
        .flatMap(memberRole ->
            roleRepository.findById(memberRole.getRoleId()))
        .collectList()
        .map(roles -> {
          // Member 객체에 역할 정보를 설정하기 위한 처리
          // 실제로는 CustomUserDetails에서 별도로 역할을 조회하므로 Member 객체 자체를 반환
          return member;
        });
  }

  /**
   * CustomUserDetails 생성
   */
  private UserDetails createUserDetails(Member member) {
    return new ReactiveCustomUserDetails(member, memberRoleRepository, roleRepository);
  }

  /**
   * 리액티브 환경을 위한 CustomUserDetails 구현
   */
  public static class ReactiveCustomUserDetails implements UserDetails {

    private final Member member;
    private final MemberRoleRepository memberRoleRepository;
    private final RoleRepository roleRepository;

    public ReactiveCustomUserDetails(Member member,
        MemberRoleRepository memberRoleRepository,
        RoleRepository roleRepository) {
      this.member = member;
      this.memberRoleRepository = memberRoleRepository;
      this.roleRepository = roleRepository;
    }

    @Override
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
      // 동기식으로 권한을 조회해야 하므로 block() 사용
      // 실제 운영에서는 다른 방식을 고려해야 함
      return memberRoleRepository.findByMemberId(member.getId())
          .flatMap(memberRole -> roleRepository.findById(memberRole.getRoleId()))
          .map(role -> new SimpleGrantedAuthority(role.getName()))
          .collectList()
          .block(); // 주의: block() 사용
    }

    @Override
    public String getPassword() {
      return member.getPassword();
    }

    @Override
    public String getUsername() {
      return member.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
      return true;
    }

    @Override
    public boolean isAccountNonLocked() {
      return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
      return true;
    }

    @Override
    public boolean isEnabled() {
      return true;
    }

    public Member getMember() {
      return member;
    }
  }
}