package com.nanoCurcuminWeb.repository;

import com.nanoCurcuminWeb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>  {
    boolean existsByEmail(String email);

    Optional<User> findByIdAndMarkAsDeletedNot(Long id, Boolean markAsDeleted);

    Optional<User> findByEmail(String email);

    Optional<User> findByUserName(String userName);

    Optional<User> findByEmailAndMarkAsDeletedNot(String email, Boolean markAsDeleted);

    Optional<User> findByVerificationToken(String token);

    List<User> findAllByMarkAsDeletedNot(Boolean markAsDeleted);
    
    // Additional convenience methods
    List<User> findByMarkAsDeletedFalse();
    
    Optional<User> findByEmailAndMarkAsDeletedFalse(String email);
    
    Optional<User> findByIdAndMarkAsDeletedFalse(Long id);
}
