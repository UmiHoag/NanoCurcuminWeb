package com.nanoCurcuminWeb.repository;

import com.nanoCurcuminWeb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>  {
    boolean existsByEmail(String email);

    Optional<User> findByIdAndMarkAsDeletedNot(Long id, String markAsDeleted);

    Optional<User> findByEmail(String email);

    Optional<User> findByUserName(String userName);

    Optional<User> findByEmailAndMarkAsDeletedNot(String email, String markAsDeleted);

    Optional<User> findByVerificationToken(String token);

    List<User> findAllByMarkAsDeletedNot(String markAsDeleted);
}
