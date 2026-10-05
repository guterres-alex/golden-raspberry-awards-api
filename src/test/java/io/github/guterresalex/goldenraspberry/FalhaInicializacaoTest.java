package io.github.guterresalex.goldenraspberry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.NestedExceptionUtils;

/*
 * Caso: a inicialização falha quando o CSV não pode ser usado.
 *
 * A carga roda em afterSingletonsInstantiated(), durante o refresh e antes de
 * o servidor web subir, e a exceção do leitor sai do run(...) sem embrulho.
 * Mesmo assim, cada teste confere o tipo e a mensagem da causa mais
 * específica, para não depender de como o Spring propaga a falha.
 *
 *   - Arquivo inexistente: datasets/nao-existe.csv não existe.
 *   - Cabeçalho inválido: datasets/cabecalho-invalido.csv tem os nomes certos,
 *     mas producers e studios trocados de posição.
 *   - Arquivo vazio: datasets/vazio.csv tem 0 bytes. É a exceção intencional à
 *     regra da quebra de linha final: um arquivo só com "\n" teria um cabeçalho
 *     vazio, e não chegaria ao caso de arquivo vazio.
 *   - Caminho sem prefixo: datasets/intervalo-zero.csv existe e é válido, então
 *     a falha vem só da falta de classpath: ou file:.
 */
class FalhaInicializacaoTest {

	@Test
	void failsWhenFileDoesNotExist() {
		assertStartupFailsWith("classpath:datasets/nao-existe.csv",
				"Arquivo CSV não encontrado: classpath:datasets/nao-existe.csv");
	}

	@Test
	void failsWhenHeaderIsInvalid() {
		assertStartupFailsWith("classpath:datasets/cabecalho-invalido.csv",
				"Cabeçalho inválido em classpath:datasets/cabecalho-invalido.csv: "
						+ "esperado 'year;title;studios;producers;winner', "
						+ "encontrado 'year;title;producers;studios;winner'");
	}

	@Test
	void failsWhenFileIsEmpty() {
		assertStartupFailsWith("classpath:datasets/vazio.csv",
				"Cabeçalho inválido em classpath:datasets/vazio.csv: arquivo vazio");
	}

	@Test
	void failsWhenPathHasNoPrefix() {
		assertStartupFailsWith("datasets/intervalo-zero.csv",
				"app.movies.csv-path deve começar com classpath: ou file: "
						+ "(valor atual: datasets/intervalo-zero.csv)");
	}

	private void assertStartupFailsWith(String csvPath, String expectedMessage) {
		// O csv-path vai como argumento de linha de comando: em .properties(...) ele
		// seria um default e o application.properties o sobrescreveria.
		// O try-with-resources fecha o contexto se a aplicação subir inesperadamente.
		IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
			try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
					GoldenRaspberryAwardsApiApplication.class)
				.properties("spring.main.web-application-type=none")
				.run("--app.movies.csv-path=" + csvPath)) {
			}
		});

		Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(thrown);
		assertInstanceOf(IllegalStateException.class, rootCause);
		assertEquals(expectedMessage, rootCause.getMessage());
	}

}
