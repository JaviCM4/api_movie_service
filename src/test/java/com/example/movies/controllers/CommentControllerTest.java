package com.example.movies.controllers;

import com.example.movies.dtos.movie.request.CreateCommentRequest;
import com.example.movies.dtos.movie.request.UpdateCommentRequest;
import com.example.movies.dtos.movie.response.CommentResponse;
import com.example.movies.dtos.movie.response.UserMovieCommentResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.services.movie.inteface.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.movies.security.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://example.com")
@Import(SecurityConfig.class)
@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final UUID MOVIE_ID = UUID.randomUUID();
    private static final UUID COMMENT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    private CommentResponse buildCommentResponse() {
        return new CommentResponse(COMMENT_ID, USER_ID, "John Doe", "Great movie!", LocalDateTime.now(), false);
    }

    // ── GET /v1/movies/{movieId}/comments ──────────────────────────────────

    @Test
    void getComments_withAuth_returnsOk() throws Exception {
        when(commentService.findCommentsByMovie(MOVIE_ID)).thenReturn(List.of(buildCommentResponse()));

        mockMvc.perform(get("/v1/movies/" + MOVIE_ID + "/comments")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Great movie!"));
    }

    @Test
    void getComments_withoutAuth_returnsOk() throws Exception {
        when(commentService.findCommentsByMovie(MOVIE_ID)).thenReturn(List.of());

        mockMvc.perform(get("/v1/movies/" + MOVIE_ID + "/comments"))
                .andExpect(status().isOk());
    }

    @Test
    void getComments_whenMovieNotFound_returnsNotFound() throws Exception {
        when(commentService.findCommentsByMovie(MOVIE_ID))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        mockMvc.perform(get("/v1/movies/" + MOVIE_ID + "/comments")
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    // ── POST /v1/movies/{movieId}/comments ─────────────────────────────────

    @Test
    void createComment_withClientRole_returnsCreated() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("Great movie!");
        when(commentService.createComment(eq(MOVIE_ID), eq(USER_ID), any(CreateCommentRequest.class)))
                .thenReturn(buildCommentResponse());

        mockMvc.perform(post("/v1/movies/" + MOVIE_ID + "/comments")
                        .with(jwt()
                                .jwt(j -> j.subject(USER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Great movie!"));
    }

    @Test
    void createComment_withBlankContent_returnsBadRequest() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("");

        mockMvc.perform(post("/v1/movies/" + MOVIE_ID + "/comments")
                        .with(jwt()
                                .jwt(j -> j.subject(USER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_whenAlreadyCommented_returnsConflict() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("Great movie!");
        when(commentService.createComment(any(), any(), any(CreateCommentRequest.class)))
                .thenThrow(new ConflictException("User already commented this movie"));

        mockMvc.perform(post("/v1/movies/" + MOVIE_ID + "/comments")
                        .with(jwt()
                                .jwt(j -> j.subject(USER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createComment_withWrongRole_returnsForbidden() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("Great movie!");

        mockMvc.perform(post("/v1/movies/" + MOVIE_ID + "/comments")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ── PATCH /v1/comments/{commentId} ─────────────────────────────────────

    @Test
    void updateComment_withClientRole_returnsOk() throws Exception {
        UpdateCommentRequest request = new UpdateCommentRequest("Updated comment");
        CommentResponse response = new CommentResponse(COMMENT_ID, USER_ID, "John Doe", "Updated comment", LocalDateTime.now(), true);
        when(commentService.updateComment(eq(COMMENT_ID), eq(USER_ID), any(UpdateCommentRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/v1/comments/" + COMMENT_ID)
                        .with(jwt()
                                .jwt(j -> j.subject(USER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated comment"))
                .andExpect(jsonPath("$.edited").value(true));
    }

    @Test
    void updateComment_whenNotFound_returnsNotFound() throws Exception {
        UpdateCommentRequest request = new UpdateCommentRequest("Updated comment");
        when(commentService.updateComment(any(), any(), any(UpdateCommentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Comment not found"));

        mockMvc.perform(patch("/v1/comments/" + COMMENT_ID)
                        .with(jwt()
                                .jwt(j -> j.subject(USER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /v1/comments/{commentId} ────────────────────────────────────

    @Test
    void deleteComment_withClientRole_returnsNoContent() throws Exception {
        doNothing().when(commentService).deleteComment(eq(COMMENT_ID), eq(USER_ID));

        mockMvc.perform(delete("/v1/comments/" + COMMENT_ID)
                        .with(jwt()
                                .jwt(j -> j.subject(USER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteComment_whenNotFound_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Comment not found"))
                .when(commentService).deleteComment(any(), any());

        mockMvc.perform(delete("/v1/comments/" + COMMENT_ID)
                        .with(jwt()
                                .jwt(j -> j.subject(USER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteComment_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/v1/comments/" + COMMENT_ID))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /v1/comments/user ───────────────────────────────────────────────

    @Test
    void getMyComments_withClientRole_returnsOk() throws Exception {
        UserMovieCommentResponse response = new UserMovieCommentResponse(
                COMMENT_ID, "Great movie!", LocalDateTime.now(), false,
                MOVIE_ID, "Forrest Gump", "http://example.com/poster.jpg");
        when(commentService.findCommentsByUser(USER_ID)).thenReturn(List.of(response));

        mockMvc.perform(get("/v1/comments/user")
                        .with(jwt()
                                .jwt(j -> j.subject(USER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Great movie!"));
    }

    @Test
    void getMyComments_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/v1/comments/user"))
                .andExpect(status().isUnauthorized());
    }
}
