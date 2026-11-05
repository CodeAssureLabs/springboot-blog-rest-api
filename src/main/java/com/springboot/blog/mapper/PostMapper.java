package com.springboot.blog.mapper;

import com.springboot.blog.entity.Post;
import com.springboot.blog.payload.PostDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    private final ModelMapper mapper;

    public PostMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    // convert Entity into DTO
    public PostDto mapToDTO(Post post){
        return mapper.map(post, PostDto.class);
    }

    // convert DTO into Entity
    public Post mapToEntity(PostDto postDto){
        return mapper.map(postDto, Post.class);
    }
}
