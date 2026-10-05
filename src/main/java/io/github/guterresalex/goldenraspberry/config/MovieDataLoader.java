package io.github.guterresalex.goldenraspberry.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import io.github.guterresalex.goldenraspberry.service.MovieCsvReader;
import io.github.guterresalex.goldenraspberry.service.MovieImportService;

@Component
public class MovieDataLoader implements ApplicationRunner {

	private final MoviesProperties properties;
	private final MovieCsvReader reader;
	private final MovieImportService importService;

	public MovieDataLoader(MoviesProperties properties, MovieCsvReader reader, MovieImportService importService) {
		this.properties = properties;
		this.reader = reader;
		this.importService = importService;
	}

	@Override
	public void run(ApplicationArguments args) {
		importService.importMovies(reader.read(properties.csvPath()));
	}

}
