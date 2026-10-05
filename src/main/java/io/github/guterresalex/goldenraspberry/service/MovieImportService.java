package io.github.guterresalex.goldenraspberry.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.guterresalex.goldenraspberry.domain.Movie;
import io.github.guterresalex.goldenraspberry.domain.Producer;
import io.github.guterresalex.goldenraspberry.domain.Studio;
import io.github.guterresalex.goldenraspberry.dto.MovieCsvRow;
import io.github.guterresalex.goldenraspberry.repository.MovieRepository;
import io.github.guterresalex.goldenraspberry.repository.ProducerRepository;
import io.github.guterresalex.goldenraspberry.repository.StudioRepository;

@Service
public class MovieImportService {

    private static final Logger log = LoggerFactory.getLogger(MovieImportService.class);

    private final MovieRepository movieRepository;
    private final ProducerRepository producerRepository;
    private final StudioRepository studioRepository;

    public MovieImportService(MovieRepository movieRepository, ProducerRepository producerRepository,
                              StudioRepository studioRepository) {
        this.movieRepository = movieRepository;
        this.producerRepository = producerRepository;
        this.studioRepository = studioRepository;
    }

    @Transactional
    public void importMovies(List<MovieCsvRow> rows) {
        List<MovieCsvRow> uniqueRows = removeDuplicates(rows);

        Map<String, Producer> producers = uniqueRows.stream()
                .flatMap(row -> row.producers().stream())
                .collect(Collectors.toMap(Producer::keyOf, Producer::new,
                        (first, second) -> first, LinkedHashMap::new));

        Map<String, Studio> studios = uniqueRows.stream()
                .flatMap(row -> row.studios().stream())
                .collect(Collectors.toMap(Studio::keyOf, Studio::new,
                        (first, second) -> first, LinkedHashMap::new));

        producerRepository.saveAll(producers.values());
        studioRepository.saveAll(studios.values());

        List<Movie> movies = uniqueRows.stream()
                .map(row -> toMovie(row, producers, studios))
                .toList();

        movieRepository.saveAll(movies);

        log.info("CSV carregado: {} filmes, {} repetidos ignorados, {} produtores, {} estúdios",
                movies.size(), rows.size() - uniqueRows.size(), producers.size(), studios.size());
    }

    private List<MovieCsvRow> removeDuplicates(List<MovieCsvRow> rows) {
        Map<MovieKey, List<MovieCsvRow>> byMovie = rows.stream()
                .collect(Collectors.groupingBy(MovieKey::of, LinkedHashMap::new, Collectors.toList()));

        byMovie.values().stream()
                .flatMap(group -> group.stream().skip(1))
                .forEach(row -> log.warn("Linha {} ignorada: filme repetido '{}' ({})",
                        row.lineNumber(), row.title(), row.year()));

        return byMovie.values().stream()
                .map(group -> group.get(0))
                .toList();
    }

    private Movie toMovie(MovieCsvRow row, Map<String, Producer> producers, Map<String, Studio> studios) {
        Movie movie = new Movie(row.year(), row.title(), row.winner());

        row.producers().stream()
                .map(name -> producers.get(Producer.keyOf(name)))
                .forEach(movie::addProducer);

        row.studios().stream()
                .map(name -> studios.get(Studio.keyOf(name)))
                .forEach(movie::addStudio);

        return movie;
    }

    private record MovieKey(String title, int year) {
        static MovieKey of(MovieCsvRow row) {
            return new MovieKey(row.title().toLowerCase(Locale.ROOT), row.year());
        }
    }

}