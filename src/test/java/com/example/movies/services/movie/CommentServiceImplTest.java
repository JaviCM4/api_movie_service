package com.example.movies.services.movie;

import com.example.movies.dtos.movie.request.CreateCommentRequest;
import com.example.movies.dtos.movie.request.UpdateCommentRequest;
import com.example.movies.dtos.movie.response.CommentResponse;
import com.example.movies.exceptions.ConflictException;
import com.example.movies.exceptions.ResourceNotFoundException;
import com.example.movies.models.movie.Movie;
import com.example.movies.models.movie.MovieComment;
import com.example.movies.repositories.movie.MovieCommentRepository;
import com.example.movies.repositories.movie.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

    private static final UUID MOVIE_ID   = UUID.randomUUID();
    private static final UUID COMMENT_ID = UUID.randomUUID();
    private static final UUID USER_ID    = UUID.randomUUID();

    @Mock private MovieCommentRepository commentRepository;
    @Mock private MovieRepository        movieRepository;

    @InjectMocks
    private CommentServiceImplementation commentService;

    // ── createComment ─────────────────────────────────────────────────────

    @Test
    void testCreateComment() throws Exception {
        // Arrange
        CommentServiceImplementation spy = spy(commentService);
        ArgumentCaptor<MovieComment> captor = ArgumentCaptor.forClass(MovieComment.class);

        CreateCommentRequest request = new CreateCommentRequest(USER_ID, "Excelente película");

        Movie movie = buildMovie();
        MovieComment saved = buildComment(movie, "Excelente película", null);

        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(commentRepository.save(any(MovieComment.class))).thenReturn(saved);

        // Act
        CommentResponse result = spy.createComment(MOVIE_ID, request);

        // Assert
        assertAll(
                () -> verify(commentRepository).save(captor.capture()),
                () -> assertEquals(USER_ID,               captor.getValue().getUserId()),
                () -> assertEquals("Excelente película", captor.getValue().getContent()),
                () -> assertEquals(COMMENT_ID,            result.getId()),
                () -> assertEquals(USER_ID,               result.getUserId()),
                () -> assertEquals("Excelente película", result.getContent()),
                () -> assertFalse(result.isEdited())
        );
    }

    @Test
    void testCreateCommentWhenMovieNotFound() {
        // Arrange
        CreateCommentRequest request = new CreateCommentRequest(USER_ID, "Excelente película");

        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> commentService.createComment(MOVIE_ID, request));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void testCreateCommentWhenCommentsNotAllowed() {
        // Arrange
        CreateCommentRequest request = new CreateCommentRequest(USER_ID, "Excelente película");

        Movie movie = buildMovie();
        movie.setAllowComments(false);

        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));

        // Assert
        assertThrows(ConflictException.class,
                () -> commentService.createComment(MOVIE_ID, request));
        verify(commentRepository, never()).save(any());
    }

    // ── updateComment ─────────────────────────────────────────────────────

    @Test
    void testUpdateComment() throws Exception {
        // Arrange
        CommentServiceImplementation spy = spy(commentService);
        ArgumentCaptor<MovieComment> captor = ArgumentCaptor.forClass(MovieComment.class);

        UpdateCommentRequest request = new UpdateCommentRequest("Contenido actualizado");

        Movie movie = buildMovie();
        MovieComment existing = buildComment(movie, "Contenido original", null);
        MovieComment updated  = buildComment(movie, "Contenido actualizado", LocalDateTime.now());

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(existing));
        when(commentRepository.save(any(MovieComment.class))).thenReturn(updated);

        // Act
        CommentResponse result = spy.updateComment(COMMENT_ID, request);

        // Assert
        assertAll(
                () -> verify(commentRepository).save(captor.capture()),
                () -> assertEquals("Contenido actualizado", captor.getValue().getContent()),
                () -> assertEquals("Contenido actualizado", result.getContent()),
                () -> assertTrue(result.isEdited())
        );
    }

    @Test
    void testUpdateCommentWhenCommentNotFound() {
        // Arrange
        UpdateCommentRequest request = new UpdateCommentRequest("Nuevo contenido");

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> commentService.updateComment(COMMENT_ID, request));
        verify(commentRepository, never()).save(any());
    }

    // ── deleteComment ─────────────────────────────────────────────────────

    @Test
    void testDeleteComment() throws Exception {
        // Arrange
        when(commentRepository.existsById(COMMENT_ID)).thenReturn(true);

        // Act
        commentService.deleteComment(COMMENT_ID);

        // Assert
        verify(commentRepository).deleteById(COMMENT_ID);
    }

    @Test
    void testDeleteCommentWhenCommentNotFound() {
        // Arrange
        when(commentRepository.existsById(COMMENT_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> commentService.deleteComment(COMMENT_ID));
        verify(commentRepository, never()).deleteById(any());
    }

    // ── findCommentsByMovie ───────────────────────────────────────────────

    @Test
    void testFindCommentsByMovie() throws Exception {
        // Arrange
        Movie movie = buildMovie();
        MovieComment c1 = buildComment(movie, "Primer comentario", null);
        MovieComment c2 = buildComment(movie, "Segundo comentario", LocalDateTime.now());
        c2.setId(UUID.randomUUID());

        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        when(commentRepository.findByMovie_IdOrderByCreatedAtDesc(MOVIE_ID))
                .thenReturn(List.of(c1, c2));

        // Act
        List<CommentResponse> result = commentService.findCommentsByMovie(MOVIE_ID);

        // Assert
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("Primer comentario",  result.get(0).getContent()),
                () -> assertFalse(result.get(0).isEdited()),
                () -> assertEquals("Segundo comentario", result.get(1).getContent()),
                () -> assertTrue(result.get(1).isEdited())
        );
    }

    @Test
    void testFindCommentsByMovieWhenMovieNotFound() {
        // Arrange
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> commentService.findCommentsByMovie(MOVIE_ID));
        verify(commentRepository, never()).findByMovie_IdOrderByCreatedAtDesc(any());
    }

    // ── Builders ──────────────────────────────────────────────────────────

    private Movie buildMovie() {
        Movie m = new Movie();
        m.setId(MOVIE_ID);
        m.setTitle("Inception");
        m.setAllowComments(true);
        m.setAllowRatings(true);
        return m;
    }

    private MovieComment buildComment(Movie movie, String content, LocalDateTime updatedAt) {
        MovieComment c = new MovieComment();
        c.setId(COMMENT_ID);
        c.setMovie(movie);
        c.setUserId(USER_ID);
        c.setContent(content);
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(updatedAt);
        return c;
    }
}
