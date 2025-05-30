package org.example.hilite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.entity.Member;
import org.example.hilite.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Member member =
        memberRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    // 예: ROLE_USER
    return org.springframework.security.core.userdetails.User.builder()
        .username(member.getUsername())
        .password(member.getPassword())
        .authorities(
            member.getMemberRoles().stream()
                .map(userRole -> userRole.getRole().getName())
                .distinct()
                .toArray(String[]::new))
        .build();
  }
}
