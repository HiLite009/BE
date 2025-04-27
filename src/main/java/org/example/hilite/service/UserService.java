package org.example.hilite.service;

import lombok.RequiredArgsConstructor;
import org.example.hilite.dto.reqeust.SignupRequestDto;
import org.example.hilite.entity.Role;
import org.example.hilite.entity.User;
import org.example.hilite.repository.RoleRepository;
import org.example.hilite.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void signup(SignupRequestDto requestDto) {
    if (!requestDto.password().equals(requestDto.passwordConfirm())) {
      throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
    }

    if (userRepository.existsByUsername(requestDto.username())) {
      throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
    }

    if (userRepository.existsByEmail(requestDto.email())) {
      throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
    }

    User user = new User();
    user.setUsername(requestDto.username());
    user.setPassword(passwordEncoder.encode(requestDto.password()));
    user.setEmail(requestDto.email());

    Role userRole =
        roleRepository
            .findByName("ROLE_USER")
            .orElseThrow(() -> new IllegalArgumentException("기본 권한이 설정되어 있지 않습니다."));

    user.getRoles().add(userRole);

    userRepository.save(user);
  }
}
