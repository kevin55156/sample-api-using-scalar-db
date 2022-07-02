package com.example.api.controller;

import com.example.api.dto.CreateMovieDto;
import com.example.api.dto.GetMovieDto;
import com.example.api.dto.MovieUserDto;
import com.example.api.service.AuthenticationService.AccountUser;
import com.example.api.service.MovieService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movies")
public class MovieController {
  private static final String PATH_MOVIE_ID = "movie_id";
  private static final String PATH_USER_ID = "user_id";

  @Autowired MovieService movieService;

  @PostMapping("/{user_id}")
  // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
  @ResponseStatus(HttpStatus.CREATED)
  public String createMovie(
      @RequestBody CreateMovieDto createMovieDto, @PathVariable(PATH_USER_ID) String userId)
      throws Exception {
    return movieService.createMovie(createMovieDto, userId);
  }

  @PutMapping("/{movie_id}/movie-users")
  // @PreAuthorize(
  //     "hasRole('ROLE_ADMIN') or @webSecurityConfig.isMovieUser(principal.movieIdList, #movieId)")
  @ResponseStatus(HttpStatus.OK)
  public void addMovieUsers(
      @PathVariable(PATH_MOVIE_ID) String movieId, @RequestBody MovieUserDto movieUser)
      throws Exception {
    movieService.addMovieUser(movieId, movieUser);
  }

  @PutMapping("/{movie_id}/movie-users/{user_id}")
  // @PreAuthorize(
  //     "hasRole('ROLE_ADMIN') or @webSecurityConfig.isMovieUser(principal.movieIdList, #movieId)")
  @ResponseStatus(HttpStatus.OK)
  public void deleteMovieUser(
      @PathVariable(PATH_MOVIE_ID) String movieId, @PathVariable(PATH_USER_ID) String userId)
      throws Exception {
    movieService.deleteMovieUser(movieId, userId);
  }

  @DeleteMapping("/{movie_id}")
  // @PreAuthorize(
  //     "hasRole('ROLE_ADMIN') or @webSecurityConfig.isMovieUser(principal.movieIdList, #movieId)")
  @ResponseStatus(HttpStatus.OK)
  public void deleteMovie(@PathVariable(PATH_MOVIE_ID) String movieId) throws Exception {
    movieService.deleteMovie(movieId);
  }

  @GetMapping("/{movie_id}/movie-users")
  // @PreAuthorize(
  //     "hasRole('ROLE_ADMIN') or @webSecurityConfig.isMovieUser(principal.movieIdList, #movieId)")
  @ResponseStatus(HttpStatus.OK)
  public List<MovieUserDto> listMovieUsers(@PathVariable(PATH_MOVIE_ID) String movieId)
      throws Exception {
    return movieService.listMovieUsers(movieId);
  }

  @GetMapping()
  // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
  @ResponseStatus(HttpStatus.OK)
  public List<GetMovieDto> listMovies() throws Exception {
    return movieService.listMovies();
  }
}
