package com.pulse.usermanagement.service;

import com.pulse.usermanagement.dto.UserRequest;
import com.pulse.usermanagement.dto.UserResponse;
import com.pulse.usermanagement.entity.User;
import com.pulse.usermanagement.entity.UserType;
import com.pulse.usermanagement.exception.BusinessRuleViolationException;
import com.pulse.usermanagement.exception.DuplicateEmailException;
import com.pulse.usermanagement.exception.UserNotFoundException;
import com.pulse.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MIN_ADMIN_AGE = 21;
    private static final long MAX_ADMIN_COUNT = 5;

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(UserRequest request) {
        validateEmailNotTaken(request.getEmail(), null);
        validateAdminRules(request.getUserType(), request.getAge(), true);

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .age(request.getAge())
                .userType(request.getUserType())
                .build();

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUserOrThrow(id);
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User existing = findUserOrThrow(id);

        validateEmailNotTaken(request.getEmail(), id);

        // Only re-check the "max admins" rule if this update turns a non-admin into an admin.
        boolean promotingToAdmin = request.getUserType() == UserType.ADMIN
                && existing.getUserType() != UserType.ADMIN;
        validateAdminRules(request.getUserType(), request.getAge(), promotingToAdmin);

        existing.setName(request.getName());
        existing.setEmail(request.getEmail());
        existing.setAge(request.getAge());
        existing.setUserType(request.getUserType());

        User saved = userRepository.save(existing);
        return UserResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);

        if (user.getUserType() == UserType.ADMIN) {
            throw new BusinessRuleViolationException(
                    "ADMIN users cannot be deleted through the User Management API",
                    HttpStatus.CONFLICT);
        }

        userRepository.delete(user);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private void validateEmailNotTaken(String email, Long currentUserId) {
        userRepository.findByEmail(email).ifPresent(existingUser -> {
            boolean belongsToSomeoneElse = currentUserId == null
                    || !existingUser.getId().equals(currentUserId);
            if (belongsToSomeoneElse) {
                throw new DuplicateEmailException(email);
            }
        });
    }

    private void validateAdminRules(UserType userType, Integer age, boolean checkMaxAdminCount) {
        if (userType != UserType.ADMIN) {
            return;
        }

        if (age < MIN_ADMIN_AGE) {
            throw new BusinessRuleViolationException(
                    "ADMIN users must be at least " + MIN_ADMIN_AGE + " years old",
                    HttpStatus.BAD_REQUEST);
        }

        if (checkMaxAdminCount && userRepository.countByUserType(UserType.ADMIN) >= MAX_ADMIN_COUNT) {
            throw new BusinessRuleViolationException(
                    "Maximum number of ADMIN users (" + MAX_ADMIN_COUNT + ") has been reached",
                    HttpStatus.CONFLICT);
        }
    }
}
