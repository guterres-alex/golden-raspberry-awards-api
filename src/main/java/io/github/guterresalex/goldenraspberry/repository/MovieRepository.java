package io.github.guterresalex.goldenraspberry.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.guterresalex.goldenraspberry.domain.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
