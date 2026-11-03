package com.springboot.blog.service.impl;

import com.springboot.blog.entity.User;
import com.springboot.blog.exception.BlogAPIException;
import com.springboot.blog.exception.ResourceNotFoundException;
import com.springboot.blog.payload.UserProfileDto;
import com.springboot.blog.repository.UserRepository;
import com.springboot.blog.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final int MAX_PAGE_SIZE = 50;

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserProfileDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToProfile(user);
    }

    @Override
    public UserProfileDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BlogAPIException(HttpStatus.NOT_FOUND, "User not found: " + username));
        return mapToProfile(user);
    }

    @Override
    public Page<UserProfileDto> searchUsers(String query, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, Math.min(pageSize, MAX_PAGE_SIZE), Sort.by("name").ascending());
        Page<User> users = StringUtils.hasText(query)
                ? userRepository.findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(query, query, pageable)
                : userRepository.findAll(pageable);
        return users.map(this::mapToProfile);
    }

    private UserProfileDto mapToProfile(User user) {
        List<String> roles = user.getRoles() == null ? List.of() : user.getRoles().stream()
                .map(role -> role.getName().replaceFirst("^ROLE_", ""))
                .sorted()
                .collect(Collectors.toList());
        return new UserProfileDto(user.getId(), user.getName(), user.getUsername(), user.getEmail(), roles);
    }
}
