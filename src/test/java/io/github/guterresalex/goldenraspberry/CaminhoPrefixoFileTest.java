package io.github.guterresalex.goldenraspberry;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Caso: o CSV é carregado por um caminho absoluto com prefixo file:, no mesmo
 * formato que o README indica ao avaliador (file:/... ou file:C:/...).
 *
 * O caminho absoluto de datasets/intervalo-zero.csv é descoberto pelo
 * classpath e passado via @DynamicPropertySource; a aplicação o carrega pelo
 * ResourceLoader como file:. O dataset e o resultado são os mesmos do
 * IntervaloZeroTest, onde está a justificativa de cada linha.
 *   - Se file: não fosse aceito, o contexto não subiria.
 *   - Se o caminho caísse no default, o resultado seria o oráculo do
 *     movielist.csv (Joel Silver e Matthew Vaughn).
 *
 * Linhas consideradas: as 4 são vencedoras e válidas.
 *
 * Vitórias por produtor:
 *   Produtor A: 2000, 2000
 *   Produtor B: 2001, 2004
 *
 * Intervalos:
 *   Produtor A: 2000 → 2000 = 0
 *   Produtor B: 2001 → 2004 = 3
 *
 * min = 0 → [Produtor A, 2000 → 2000]
 * max = 3 → [Produtor B, 2001 → 2004]
 */
@SpringBootTest
@AutoConfigureMockMvc
class CaminhoPrefixoFileTest {

	@Autowired
	private MockMvc mockMvc;

	@DynamicPropertySource
	static void csvPath(DynamicPropertyRegistry registry) throws IOException {
		String absolutePath = new ClassPathResource("datasets/intervalo-zero.csv").getFile()
			.getAbsolutePath()
			.replace('\\', '/');
		registry.add("app.movies.csv-path", () -> "file:" + absolutePath);
	}

	@Test
	void loadsCsvFromAbsoluteFilePath() throws Exception {
		String expected = """
				{
				  "min": [
				    {"producer": "Produtor A", "interval": 0, "previousWin": 2000, "followingWin": 2000}
				  ],
				  "max": [
				    {"producer": "Produtor B", "interval": 3, "previousWin": 2001, "followingWin": 2004}
				  ]
				}
				""";

		mockMvc.perform(get("/api/producers/award-intervals"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json(expected, JsonCompareMode.STRICT));
	}

}
