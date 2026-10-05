package io.github.guterresalex.goldenraspberry.config;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import io.github.guterresalex.goldenraspberry.service.MovieCsvReader;
import io.github.guterresalex.goldenraspberry.service.MovieImportService;

@Component
public class MovieDataLoader implements SmartInitializingSingleton {

	private final MoviesProperties properties;
	private final MovieCsvReader reader;
	private final MovieImportService importService;

	public MovieDataLoader(MoviesProperties properties, MovieCsvReader reader, MovieImportService importService) {
		this.properties = properties;
		this.reader = reader;
		this.importService = importService;
	}

	@Override
	public void afterSingletonsInstantiated() {
		importService.importMovies(reader.read(properties.csvPath()));
	}

}
