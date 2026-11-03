package com.springboot.blog.service;

import com.springboot.blog.payload.UserProfileDto;
import org.springframework.data.domain.Page;

public interface UserService {

    UserProfileDto getUserById(Long userId);

    UserProfileDto getUserByUsername(String username);

    Page<UserProfileDto> searchUsers(String query, int pageNo, int pageSize);
}
