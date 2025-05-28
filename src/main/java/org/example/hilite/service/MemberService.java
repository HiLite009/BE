package org.example.hilite.service;

import lombok.RequiredArgsConstructor;
import org.example.hilite.dto.reqeust.SignupRequestDto;
import org.example.hilite.entity.Role;
import org.example.hilite.entity.Member;
import org.example.hilite.repository.RoleRepository;
import org.example.hilite.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void signup(SignupRequestDto requestDto) {
    if (!requestDto.password().equals(requestDto.passwordConfirm())) {
      throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
    }

    if (memberRepository.existsByUsername(requestDto.username())) {
      throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
    }

    if (memberRepository.existsByEmail(requestDto.email())) {
      throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
    }

    Member member = new Member();
    member.setUsername(requestDto.username());
    member.setPassword(passwordEncoder.encode(requestDto.password()));
    member.setEmail(requestDto.email());

    Role userRole =
        roleRepository
            .findByName("ROLE_USER")
            .orElseThrow(() -> new IllegalArgumentException("기본 권한이 설정되어 있지 않습니다."));

    member.addRole(userRole);

    memberRepository.save(member);
  }

  @Transactional(readOnly = true)
  public boolean checkEmail(String email) {
    return memberRepository.existsByEmail(email);
  }
}
