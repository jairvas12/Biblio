package com.library.user_service.service.impl;

import com.library.user_service.dto.UserRequestDTO;
import com.library.user_service.dto.UserResponseDTO;
import com.library.user_service.exception.EmailAlreadyExistsException;
import com.library.user_service.exception.InvalidRoleException;
import com.library.user_service.exception.UserNotFoundException;
import com.library.user_service.model.Role;
import com.library.user_service.model.User;
import com.library.user_service.repository.UserRepository;
import com.library.user_service.service.UserService;
import com.library.user_service.dto.UserUpdateDTO;
import com.library.user_service.exception.EmailChangeNotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponseDTO createUser(
            UserRequestDTO requestDTO
    ) {

        String normalizedEmail =
                normalizeEmail(requestDTO.getEmail());

        log.info(
                "Creating user with email: {}",
                normalizedEmail
        );

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {

            log.warn(
                    "Creation rejected because email already exists: {}",
                    normalizedEmail
            );

            throw new EmailAlreadyExistsException(
                    "Ya existe un usuario registrado con el correo: "
                            + normalizedEmail
            );
        }

        Role role = parseRole(requestDTO.getRole());

        User user = User.builder()
                .name(normalizeName(requestDTO.getName()))
                .email(normalizedEmail)
                .role(role)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        log.info(
                "User created successfully with id: {}",
                savedUser.getId()
        );

        return mapToDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {

        log.info("Fetching all users");

        List<UserResponseDTO> users =
                userRepository.findAll()
                        .stream()
                        .map(this::mapToDTO)
                        .toList();

        log.info(
                "Total users found: {}",
                users.size()
        );

        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(
            String email
    ) {

        String normalizedEmail = normalizeEmail(email);

        log.info(
                "Searching user with email: {}",
                normalizedEmail
        );

        User user = userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> {

                    log.warn(
                            "User not found with email: {}",
                            normalizedEmail
                    );

                    return new UserNotFoundException(
                            "No se encontró un usuario con el correo: "
                                    + normalizedEmail
                    );
                });

        return mapToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(
            Long id
    ) {

        log.info(
                "Searching user with id: {}",
                id
        );

        User user = findUserById(id);

        return mapToDTO(user);
    }

    @Override
    public UserResponseDTO updateUser(
            Long id,
            UserUpdateDTO requestDTO
    ) {

        log.info(
                "Updating user with id: {}",
                id
        );

        User user = findUserById(id);

        String requestedEmail =
                normalizeEmail(
                        requestDTO.getEmail()
                );

        if (!user.getEmail()
                .equalsIgnoreCase(requestedEmail)) {

            log.warn(
                    "Direct email change rejected for user id {}. " +
                    "Current email: {}, requested email: {}",
                    id,
                    user.getEmail(),
                    requestedEmail
            );

            throw new EmailChangeNotAllowedException(
                    "El correo no puede modificarse directamente " +
                    "desde el servicio de usuarios"
            );
        }

        user.setName(
                normalizeName(
                        requestDTO.getName()
                )
        );

        user.setRole(
                parseRole(
                        requestDTO.getRole()
                )
        );

        User updatedUser =
                userRepository.save(user);

        log.info(
                "User updated successfully with id: {}",
                updatedUser.getId()
        );

        return mapToDTO(updatedUser);
    }

    @Override
    public UserResponseDTO updateEmailInternally(
            Long id,
            String newEmail
    ) {

        log.info(
                "Processing internal email update for user id: {}",
                id
        );

        User user = findUserById(id);

        String normalizedEmail =
                normalizeEmail(newEmail);

        if (user.getEmail()
                .equalsIgnoreCase(normalizedEmail)) {

            log.info(
                    "The requested email is already assigned to user id: {}",
                    id
            );

            return mapToDTO(user);
        }

        boolean emailUsedByAnotherUser =
                userRepository
                        .existsByEmailIgnoreCaseAndIdNot(
                                normalizedEmail,
                                id
                        );

        if (emailUsedByAnotherUser) {

            log.warn(
                    "Internal email update rejected. Email {} belongs to another user",
                    normalizedEmail
            );

            throw new EmailAlreadyExistsException(
                    "El correo ya pertenece a otro usuario: "
                            + normalizedEmail
            );
        }

        String previousEmail =
                user.getEmail();

        user.setEmail(normalizedEmail);

        User updatedUser =
                userRepository.save(user);

        log.info(
                "Email updated internally for user id {}. Previous email: {}, new email: {}",
                id,
                previousEmail,
                normalizedEmail
        );

        return mapToDTO(updatedUser);
    }
    @Override
    public void deleteUser(
            Long id
    ) {

        log.info(
                "Deactivating user with id: {}",
                id
        );

        User user = findUserById(id);

        if (!Boolean.TRUE.equals(user.getActive())) {

            log.info(
                    "User with id {} was already inactive",
                    id
            );

            return;
        }

        user.setActive(false);

        userRepository.save(user);

        log.info(
                "User deactivated successfully with id: {}",
                id
        );
    }

    private User findUserById(
            Long id
    ) {

        return userRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn(
                            "User not found with id: {}",
                            id
                    );

                    return new UserNotFoundException(
                            "No se encontró un usuario con el id: "
                                    + id
                    );
                });
    }

    private Role parseRole(
            String role
    ) {

        try {

            return Role.valueOf(
                    role.trim()
                            .toUpperCase(Locale.ROOT)
            );

        } catch (IllegalArgumentException |
                 NullPointerException ex) {

            throw new InvalidRoleException(
                    "El rol debe ser ADMIN, BIBLIOTECARIO o USER"
            );
        }
    }

    private String normalizeEmail(
            String email
    ) {

        return email.trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeName(
            String name
    ) {

        return name.trim()
                .replaceAll("\\s+", " ");
    }

    private UserResponseDTO mapToDTO(
            User user
    ) {

        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getActive())
                .build();
    }
}