package com.nanoCurcuminWeb.repository;

import com.nanoCurcuminWeb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    User findByIdAndMarkAsDeletedNot(Long id, String markAsDeleted);
    User findByEmail(String email);
}
