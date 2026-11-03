package com.springboot.blog.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Public-facing author profile used by the post byline and the author pages.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String name;
    private String username;
    private String email;
    // e.g. ["ADMIN"] - lets the UI render a "Staff" badge next to the author name
    private List<String> roles;
}
