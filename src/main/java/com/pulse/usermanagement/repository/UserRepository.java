package com.pulse.usermanagement.repository;

import com.pulse.usermanagement.entity.User;
import com.pulse.usermanagement.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByUserType(UserType userType);
}
