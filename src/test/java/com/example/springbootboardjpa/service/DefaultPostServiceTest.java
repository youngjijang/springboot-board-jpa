package com.example.springbootboardjpa.service;

import com.example.springbootboardjpa.dto.PostDTO;
import com.example.springbootboardjpa.exception.NotFoundException;
import com.example.springbootboardjpa.model.Post;
import com.example.springbootboardjpa.model.User;
import com.example.springbootboardjpa.repoistory.PostJpaRepository;
import com.example.springbootboardjpa.repoistory.UserJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ActiveProfiles("test")
@SpringBootTest
@Slf4j
@Transactional
class DefaultPostServiceTest {

    @Autowired
    private DefaultPostService postService;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private PostJpaRepository postRepository;

    private User user;

    @BeforeEach
    public void setUp() {
        postRepository.deleteAll();
        userRepository.deleteAll();
        user = new User("영지", 30);
        user = userRepository.save(user);
    }

    @Test
    @DisplayName("post가 정상 저장된디.")
    public void saveTest() {
        // Given
        var postDto = new PostDTO.Save("test_title", "content", user.getId());

        // When
        var saveId = postService.save(postDto);

        // Then
        assertThat(postRepository.findAll().size()).isEqualTo(1);
        assertThat(postRepository.findById(saveId).isPresent()).isTrue();
        assertThat(postRepository.findById(saveId).get().getTitle()).isEqualTo("test_title");

    }

    @Test
    @DisplayName("post를 id로 조회할 수 있다.")
    public void findByIdTest() {
        // Given
        var post = new Post("test_title", "content", user);
        var save = postRepository.save(post);

        // When
        var responseDto = postService.findById(save.getId());

        // Then
        assertThat(responseDto.getId()).isEqualTo(save.getId());
        assertThat(responseDto.getTitle()).isEqualTo(post.getTitle());
        assertThat(responseDto.getContent()).isEqualTo(post.getContent());
        assertThat(responseDto.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 id를 조회할 경우  NotFoundException을 발생시킨다.")
    public void findFailByIdTest() {
        // Given
        assertThat(postRepository.findById(0L).isEmpty()).isTrue();

        // When// Then
        assertThrows(NotFoundException.class, () -> postService.findById(0L));
    }

    @Test
    @DisplayName("post를 정상 변경할 수 있다.")
    public void updateTest() {
        // Given
        var post = new Post("test_title", "content", user);
        var save = postRepository.save(post);

        // When
        postService.update(save.getId(), "update_title", "update_content");

        // Then
        assertThat(postRepository.findById(save.getId()).isPresent()).isTrue();
        assertThat(postRepository.findById(save.getId()).get().getTitle()).isEqualTo("update_title");
        assertThat(postRepository.findById(save.getId()).get().getContent()).isEqualTo("update_content");
    }

    @Test
    @DisplayName("post title이 빈칸일때 (제목없음)으로 정상 변경한다.")
    public void updateBlankTitleTest() {
        // Given
        var post = new Post("test_title", "content", user);
        var save = postRepository.save(post);

        // When
        postService.update(save.getId(), "", "update_content");

        // Then
        assertThat(postRepository.findById(save.getId()).isPresent()).isTrue();
        assertThat(postRepository.findById(save.getId()).get().getTitle()).isEqualTo("(제목없음)");
        assertThat(postRepository.findById(save.getId()).get().getContent()).isEqualTo("update_content");
    }

    @Test
    @DisplayName("post title이 50자 이상인 경우 변경에 실패한다.")
    public void updateFailTest() {
        // Given
        var post = new Post("test_title", "content", user);
        var save = postRepository.saveAndFlush(post);

        // When// Then
        assertThatThrownBy(() ->
                postService.update(save.getId(), "012345678901234567890123456789012345678901234567891", "update_content"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제목은 50자 이하여야합니다.");

        Post findPost = postRepository.findById(save.getId()).get();
        assertThat(findPost.getTitle()).isEqualTo("test_title");
        assertThat(findPost.getContent()).isEqualTo("content");
    }

    @Test
    @DisplayName("모든 post를 page를 조회할 수 있다.")
    public void findAllTest() {
        // Given
        List<Post> posts = Arrays.asList(new Post("test_title0", "content0", user),
                new Post("test_title1", "content1", user),
                new Post("test_title2", "content2", user),
                new Post("test_title3", "content3", user),
                new Post("test_title4", "content4", user),
                new Post("test_title5", "content5", user),
                new Post("test_title6", "content6", user));
        postRepository.saveAll(posts);
        PageRequest pageable = PageRequest.of(0, 3);

        // When
        var findPosts = postService.findAll(pageable);

        // Then
        assertThat(findPosts.getTotalElements()).isEqualTo(7);
        assertThat(findPosts.getContent().size()).isEqualTo(3);
        assertThat(findPosts.getNumber()).isEqualTo(0);
        assertThat(findPosts.getTotalPages()).isEqualTo(3);
        assertThat(findPosts.isFirst()).isTrue();
        assertThat(findPosts.hasNext()).isTrue();
    }
}