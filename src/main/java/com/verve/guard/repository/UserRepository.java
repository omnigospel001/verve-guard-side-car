package com.verve.guard.repository;

import com.verve.guard.entity.User;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @NullMarked
    Page<User> findAll(Pageable pageable);

    Optional<User> findByAccountNumber(Long accountNumber);

    Optional<User> findByEmail(String email);

}
