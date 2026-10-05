package io.github.guterresalex.goldenraspberry.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.movies")
public record MoviesProperties(String csvPath) {
}
