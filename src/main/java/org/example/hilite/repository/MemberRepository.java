package org.example.hilite.repository;

import java.util.Optional;
import org.example.hilite.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);
}
