package com.verve.guard.repository;

import com.verve.guard.entity.AccountManagement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountManagementRepository extends JpaRepository<AccountManagement, Long> {
}
