package io.github.guterresalex.goldenraspberry;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Caso: um produtor repetido no mesmo filme gera um único vínculo.
 *
 * Linhas consideradas: as 2 são vencedoras e válidas, com títulos distintos.
 *   - "Ana, Ana" repete a grafia exata; "Ana and ANA" repete a chave "ana".
 *     Cada filme gera um único vínculo com Ana.
 *   - Com vínculo duplicado, a query devolveria cada ano duas vezes e o min
 *     teria dois pares com intervalo 0.
 *
 * Vitórias por produtor:
 *   Ana: 2000, 2007
 *
 * Intervalos:
 *   Ana: 2000 → 2007 = 7
 *
 * min = max = 7 → Ana (2000) nas duas listas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/produtor-repetido-mesmo-filme.csv")
class ProdutorRepetidoMesmoFilmeTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void linksRepeatedProducerOnlyOncePerMovie() throws Exception {
		String expected = """
				{
				  "min": [
				    {"producer": "Ana", "interval": 7, "previousWin": 2000, "followingWin": 2007}
				  ],
				  "max": [
				    {"producer": "Ana", "interval": 7, "previousWin": 2000, "followingWin": 2007}
				  ]
				}
				""";

		mockMvc.perform(get("/api/producers/award-intervals"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json(expected, JsonCompareMode.STRICT));
	}

}
