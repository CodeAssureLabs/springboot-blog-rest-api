package com.springboot.blog.controller;

import com.springboot.blog.payload.UserProfileDto;
import com.springboot.blog.service.UserService;
import com.springboot.blog.utils.AppConstants;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Author profiles: powers the byline link on each post and the "Authors" page.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Build Search Authors REST API
    @GetMapping
    public ResponseEntity<Page<UserProfileDto>> searchUsers(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize
    ){
        return ResponseEntity.ok(userService.searchUsers(query, pageNo, pageSize));
    }

    // Build Get Author By Id REST API
    @GetMapping("{id}")
    public ResponseEntity<UserProfileDto> getUserById(@PathVariable("id") Long userId){
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // Build Get Author By Username REST API
    @GetMapping("by-username/{username}")
    public ResponseEntity<UserProfileDto> getUserByUsername(@PathVariable("username") String username){
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }
}
