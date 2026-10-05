package io.github.guterresalex.goldenraspberry.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import io.github.guterresalex.goldenraspberry.domain.ColumnLimits;
import io.github.guterresalex.goldenraspberry.dto.MovieCsvRow;

@Component
public class MovieCsvReader {

	private static final Logger log = LoggerFactory.getLogger(MovieCsvReader.class);

	private static final String SEPARATOR = ";";
	private static final List<String> HEADER = List.of("year", "title", "studios", "producers", "winner");
	private static final Pattern NAME_SEPARATOR = Pattern.compile(",\\s*and\\s+|,|\\s+and\\s+");

	private final ResourceLoader resourceLoader;

	public MovieCsvReader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public List<MovieCsvRow> read(String csvPath) {
		if (csvPath == null || !(csvPath.startsWith("classpath:") || csvPath.startsWith("file:"))) {
			throw new IllegalStateException(
					"app.movies.csv-path deve começar com classpath: ou file: (valor atual: " + csvPath + ")");
		}
		Resource resource = resourceLoader.getResource(csvPath);
		if (!resource.exists()) {
			throw new IllegalStateException("Arquivo CSV não encontrado: " + csvPath);
		}

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
			validateHeader(reader.readLine(), csvPath);

			List<MovieCsvRow> rows = new ArrayList<>();
			int ignoredLines = 0;
			int lineNumber = 1;
			String line;
			while ((line = reader.readLine()) != null) {
				lineNumber++;
				if (line.isBlank()) {
					continue;
				}
				MovieCsvRow row = parseLine(line, lineNumber);
				if (row != null) {
					rows.add(row);
				}
				else {
					ignoredLines++;
				}
			}
			log.info("CSV lido: {} linhas válidas, {} linhas malformadas ignoradas", rows.size(), ignoredLines);
			return rows;
		}
		catch (IOException e) {
			throw new UncheckedIOException("Falha ao ler o arquivo CSV: " + csvPath, e);
		}
	}

	private void validateHeader(String header, String csvPath) {
		if (header == null) {
			throw new IllegalStateException("Cabeçalho inválido em " + csvPath + ": arquivo vazio");
		}
		if (header.startsWith("\uFEFF")) {
			header = header.substring(1);
		}
		String[] columns = header.split(SEPARATOR, -1);
		boolean valid = columns.length == HEADER.size();
		for (int i = 0; valid && i < columns.length; i++) {
			valid = columns[i].trim().equalsIgnoreCase(HEADER.get(i));
		}
		if (!valid) {
			throw new IllegalStateException("Cabeçalho inválido em " + csvPath + ": esperado '"
					+ String.join(SEPARATOR, HEADER) + "', encontrado '" + header + "'");
		}
	}

	private MovieCsvRow parseLine(String line, int lineNumber) {
		String[] fields = Arrays.stream(line.split(SEPARATOR, -1)).map(String::trim).toArray(String[]::new);
		if (fields.length != HEADER.size()) {
			log.warn("Linha {} ignorada: esperadas {} colunas, encontradas {}", lineNumber, HEADER.size(),
					fields.length);
			return null;
		}

		int year;
		try {
			year = Integer.parseInt(fields[0]);
		}
		catch (NumberFormatException e) {
			log.warn("Linha {} ignorada: ano inválido '{}'", lineNumber, fields[0]);
			return null;
		}

		String title = fields[1];
		if (title.isEmpty()) {
			log.warn("Linha {} ignorada: título vazio", lineNumber);
			return null;
		}

		if (title.length() > ColumnLimits.TEXT_MAX_LENGTH) {
			log.warn("Linha {} ignorada: título excede {} caracteres", lineNumber, ColumnLimits.TEXT_MAX_LENGTH);
			return null;
		}

		List<String> producers = splitNames(fields[3]);
		if (producers.isEmpty()) {
			log.warn("Linha {} ignorada: nenhum produtor", lineNumber);
			return null;
		}
		if (exceedsMaxLength(producers)) {
			log.warn("Linha {} ignorada: nome de produtor excede {} caracteres", lineNumber,
					ColumnLimits.TEXT_MAX_LENGTH);
			return null;
		}

		List<String> studios = splitNames(fields[2]);
		if (exceedsMaxLength(studios)) {
			log.warn("Linha {} ignorada: nome de estúdio excede {} caracteres", lineNumber,
					ColumnLimits.TEXT_MAX_LENGTH);
			return null;
		}

		boolean winner = "yes".equalsIgnoreCase(fields[4]);
		return new MovieCsvRow(year, title, studios, producers, winner, lineNumber);
	}

	private List<String> splitNames(String field) {
		return NAME_SEPARATOR.splitAsStream(field).map(String::trim).filter(name -> !name.isEmpty()).toList();
	}

	private boolean exceedsMaxLength(List<String> names) {
		return names.stream().anyMatch(name -> name.length() > ColumnLimits.TEXT_MAX_LENGTH);
	}

}
