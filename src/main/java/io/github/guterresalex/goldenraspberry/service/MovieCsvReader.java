package io.github.guterresalex.goldenraspberry.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import io.github.guterresalex.goldenraspberry.domain.ColumnLimits;
import io.github.guterresalex.goldenraspberry.dto.MovieCsvRow;

import static java.util.Objects.isNull;

@Component
public class MovieCsvReader {

    private static final Logger log = LoggerFactory.getLogger(MovieCsvReader.class);

    private static final String WINNER_RESULT = "yes";
    private static final String SEPARATOR = ";";
    private static final String BOM = "\uFEFF";
    private static final int FIRST_DATA_LINE = 2;
    private static final List<String> ALLOWED_PREFIXES = List.of("classpath:", "file:");
    private static final List<String> HEADER = List.of("year", "title", "studios", "producers", "winner");
    private static final Pattern NAME_SEPARATOR = Pattern.compile(",\\s*and\\s+|,|\\s+and\\s+");

    private final ResourceLoader resourceLoader;

    public MovieCsvReader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public List<MovieCsvRow> read(String csvPath) {
        Resource resource = loadResource(csvPath);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            validateHeader(reader.readLine(), csvPath);
            List<String> lines = reader.lines().toList();

            List<Optional<MovieCsvRow>> parsed = IntStream.range(0, lines.size())
                    .filter(i -> !lines.get(i).isBlank())
                    .mapToObj(i -> parseLine(lines.get(i), i + FIRST_DATA_LINE))
                    .toList();

            List<MovieCsvRow> rows = parsed.stream()
                    .flatMap(Optional::stream)
                    .toList();

            log.info("CSV lido: {} linhas válidas, {} linhas malformadas ignoradas", rows.size(), parsed.size() - rows.size());

            return rows;
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo CSV: " + csvPath, e);
        } catch (UncheckedIOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo CSV: " + csvPath, e.getCause());
        }
    }

    private Resource loadResource(String csvPath) {
        if (isNull(csvPath) || ALLOWED_PREFIXES.stream().noneMatch(csvPath::startsWith)) {
            throw new IllegalStateException(
                    "app.movies.csv-path deve começar com classpath: ou file: (valor atual: " + csvPath + ")");
        }

        Resource resource = resourceLoader.getResource(csvPath);
        if (!resource.exists()) {
            throw new IllegalStateException("Arquivo CSV não encontrado: " + csvPath);
        }

        return resource;
    }

    private void validateHeader(String rawHeader, String csvPath) {
        if (isNull(rawHeader)) {
            throw new IllegalStateException("Cabeçalho inválido em " + csvPath + ": arquivo vazio");
        }

        String header = rawHeader.startsWith(BOM) ? rawHeader.substring(1) : rawHeader;

        List<String> columns = Arrays.stream(header.split(SEPARATOR, -1))
                .map(column -> column.trim().toLowerCase(Locale.ROOT))
                .toList();

        if (!columns.equals(HEADER)) {
            throw new IllegalStateException("Cabeçalho inválido em " + csvPath + ": esperado '"
                    + String.join(SEPARATOR, HEADER) + "', encontrado '" + header + "'");
        }
    }

    private Optional<MovieCsvRow> parseLine(String line, int lineNumber) {
        String[] fields = Arrays.stream(line.split(SEPARATOR, -1)).map(String::trim).toArray(String[]::new);

        if (fields.length != HEADER.size()) {
            return skip(lineNumber, "esperadas %d colunas, encontradas %d".formatted(HEADER.size(), fields.length));
        }

        int year;
        try {
            year = Integer.parseInt(fields[0]);
        } catch (NumberFormatException e) {
            return skip(lineNumber, "ano inválido '%s'".formatted(fields[0]));
        }

        String title = fields[1];
        if (title.isEmpty()) {
            return skip(lineNumber, "título vazio");
        }
        if (tooLong(title)) {
            return skip(lineNumber, "título excede %d caracteres".formatted(ColumnLimits.TEXT_MAX_LENGTH));
        }

        List<String> producers = splitNames(fields[3]);
        if (producers.isEmpty()) {
            return skip(lineNumber, "nenhum produtor");
        }
        if (anyTooLong(producers)) {
            return skip(lineNumber, "nome de produtor excede %d caracteres".formatted(ColumnLimits.TEXT_MAX_LENGTH));
        }

        List<String> studios = splitNames(fields[2]);
        if (anyTooLong(studios)) {
            return skip(lineNumber, "nome de estúdio excede %d caracteres".formatted(ColumnLimits.TEXT_MAX_LENGTH));
        }

        boolean winner = WINNER_RESULT.equalsIgnoreCase(fields[4]);

        return Optional.of(new MovieCsvRow(year, title, studios, producers, winner, lineNumber));
    }

    private Optional<MovieCsvRow> skip(int lineNumber, String reason) {
        log.warn("Linha {} ignorada: {}", lineNumber, reason);
        return Optional.empty();
    }

    private List<String> splitNames(String field) {
        return NAME_SEPARATOR.splitAsStream(field)
				.map(String::trim)
				.filter(name -> !name.isEmpty())
				.toList();
    }

    private static boolean anyTooLong(List<String> names) {
        return names.stream().anyMatch(MovieCsvReader::tooLong);
    }

    private static boolean tooLong(String value) {
        return value.length() > ColumnLimits.TEXT_MAX_LENGTH;
    }

}