package io.github.guterresalex.goldenraspberry.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
		Set<MovieKey> movieKeys = new HashSet<>();
		Map<String, Producer> producers = new HashMap<>();
		Map<String, Studio> studios = new HashMap<>();
		int movieCount = 0;
		int duplicateCount = 0;

		for (MovieCsvRow row : rows) {
			if (!movieKeys.add(new MovieKey(row.title().toLowerCase(Locale.ROOT), row.year()))) {
				log.warn("Linha {} ignorada: filme repetido '{}' ({})", row.lineNumber(), row.title(), row.year());
				duplicateCount++;
				continue;
			}

			Movie movie = new Movie(row.year(), row.title(), row.winner());
			for (String name : row.producers()) {
				movie.addProducer(producers.computeIfAbsent(Producer.keyOf(name),
						key -> producerRepository.save(new Producer(name))));
			}
			for (String name : row.studios()) {
				movie.addStudio(studios.computeIfAbsent(Studio.keyOf(name),
						key -> studioRepository.save(new Studio(name))));
			}
			movieRepository.save(movie);
			movieCount++;
		}

		log.info("CSV carregado: {} filmes, {} repetidos ignorados, {} produtores, {} estúdios", movieCount,
				duplicateCount, producers.size(), studios.size());
	}

	private record MovieKey(String title, int year) {
	}

}
