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
import com.example.movies.services.movie.inteface.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CommentServiceImplementation implements CommentService {

    private final MovieCommentRepository commentRepository;
    private final MovieRepository movieRepository;

    public CommentServiceImplementation(MovieCommentRepository commentRepository, MovieRepository movieRepository) {
        this.commentRepository = commentRepository;
        this.movieRepository = movieRepository;
    }

    @Override
    @Transactional
    public CommentResponse createComment(UUID movieId, CreateCommentRequest dto) throws ResourceNotFoundException, ConflictException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        if (!movie.isAllowComments()) {
            throw new ConflictException("Comments are not allowed for this movie");
        }

        MovieComment comment = dto.createEntity();
        comment.setMovie(movie);
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentResponse updateComment(UUID commentId, UpdateCommentRequest dto) throws ResourceNotFoundException {
        MovieComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        comment.setContent(dto.getContent());
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) throws ResourceNotFoundException {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> findCommentsByMovie(UUID movieId) throws ResourceNotFoundException {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with id: " + movieId);
        }
        return commentRepository.findByMovie_IdOrderByCreatedAtDesc(movieId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }
}
