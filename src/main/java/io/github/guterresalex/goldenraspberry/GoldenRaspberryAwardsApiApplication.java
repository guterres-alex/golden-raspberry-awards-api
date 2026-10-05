package io.github.guterresalex.goldenraspberry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import io.github.guterresalex.goldenraspberry.config.MoviesProperties;

@SpringBootApplication
@EnableConfigurationProperties(MoviesProperties.class)
public class GoldenRaspberryAwardsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoldenRaspberryAwardsApiApplication.class, args);
	}

}
