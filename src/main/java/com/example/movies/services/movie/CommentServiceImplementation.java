package com.example.movies.services.movie;

import com.example.movies.client.tickets.TicketsClient;
import com.example.movies.client.users.UserClient;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CommentServiceImplementation implements CommentService {

    private final MovieCommentRepository commentRepository;
    private final MovieRepository movieRepository;
    private final TicketsClient ticketsClient;
    private final UserClient userClient;

    @Autowired
    public CommentServiceImplementation(MovieCommentRepository commentRepository, MovieRepository movieRepository,
                                        TicketsClient ticketsClient, UserClient userClient) {
        this.commentRepository = commentRepository;
        this.movieRepository = movieRepository;
        this.ticketsClient = ticketsClient;
        this.userClient = userClient;
    }

    @Override
    @Transactional
    public CommentResponse createComment(UUID movieId, UUID userId, CreateCommentRequest dto)
            throws ResourceNotFoundException, ConflictException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Película no encontrada con id: " + movieId));

        if (!movie.isAllowComments()) {
            throw new ConflictException("Los comentarios no están permitidos para esta película");
        }

        //Validar que el usuario ya tenga (haya visto la pelicula) una entrada para la pelicula antes de permitirle comentar
        if (!ticketsClient.hasTicketsByMovieAndUser(movieId, userId)) {
            throw new ConflictException("No puedes comentar esta película porque no has comprado entradas para ella");
        }

        MovieComment comment = dto.createEntity(userId);
        comment.setMovie(movie);
        MovieComment saved = commentRepository.save(comment);
        return CommentResponse.from(saved, userClient.getUserName(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentResponse updateComment(UUID commentId, UUID userId, UpdateCommentRequest dto)
            throws ResourceNotFoundException, ConflictException {
        MovieComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con id: " + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new ConflictException("No tienes permiso para modificar este comentario porque fue creado por otro usuario");
        }

        comment.setContent(dto.getContent());
        MovieComment saved = commentRepository.save(comment);
        return CommentResponse.from(saved, userClient.getUserName(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(UUID commentId, UUID userId)
            throws ResourceNotFoundException, ConflictException {
        MovieComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con id: " + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new ConflictException("No tienes permiso para eliminar este comentario porque fue creado por otro usuario");
        }

        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> findCommentsByMovie(UUID movieId)
            throws ResourceNotFoundException {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Película no encontrada con id: " + movieId);
        }
        return commentRepository.findByMovie_IdOrderByCreatedAtDesc(movieId)
                .stream()
                .map(comment -> CommentResponse.from(comment, userClient.getUserName(comment.getUserId())))
                .toList();
    }
}
