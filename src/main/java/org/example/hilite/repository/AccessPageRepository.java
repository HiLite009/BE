package org.example.hilite.repository;

import java.util.Optional;
import org.example.hilite.entity.AccessPage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessPageRepository extends JpaRepository<AccessPage, Long> {
  Optional<AccessPage> findByPath(String path);
}
