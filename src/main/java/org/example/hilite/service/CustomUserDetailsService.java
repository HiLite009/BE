package org.example.hilite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.config.CustomUserDetails;
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
    log.debug("Loading user by username: {}", username);

    Member member =
        memberRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

    log.debug(
        "Found member: {} with roles: {}",
        member.getUsername(),
        member.getMemberRoles().stream().map(mr -> mr.getRole().getName()).toList());

    return new CustomUserDetails(member);
  }
}
