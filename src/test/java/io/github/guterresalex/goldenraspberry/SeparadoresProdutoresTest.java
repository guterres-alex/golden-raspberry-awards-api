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
 * Caso: produtores separados por ",", " and " e ", and " são divididos em nomes
 * distintos, e cada separador influencia o resultado.
 *
 * Linhas consideradas: as 3 são vencedoras e válidas, com títulos distintos.
 *   - "Ana, Bia"           → Ana, Bia
 *   - "Bia and Caio"       → Bia, Caio
 *   - "Ana, Bia, and Caio" → Ana, Bia, Caio
 *   - Sem dividir por "," (mantendo ", and "), surge o produtor "Ana, Bia"
 *     (2000 → 2004): max = 4 ("Ana, Bia") e min = 1 só com Caio.
 *   - Sem dividir por " and ", surge "Bia and Caio" e min = max = 4.
 *   - Dividindo por "," antes de " and ", sobra "and Caio" e Caio sai do min.
 *
 * Vitórias por produtor:
 *   Ana: 2000, 2004
 *   Bia: 2000, 2003, 2004
 *   Caio: 2003, 2004
 *
 * Intervalos:
 *   Ana: 2000 → 2004 = 4
 *   Bia: 2000 → 2003 = 3, 2003 → 2004 = 1
 *   Caio: 2003 → 2004 = 1
 *
 * min = 1 → Bia (2003), Caio (2003); mesmo previousWin, desempate pelo nome.
 * max = 4 → Ana (2000)
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/separadores-produtores.csv")
class SeparadoresProdutoresTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void splitsProducersByCommaAndAndCommaAnd() throws Exception {
		String expected = """
				{
				  "min": [
				    {"producer": "Bia", "interval": 1, "previousWin": 2003, "followingWin": 2004},
				    {"producer": "Caio", "interval": 1, "previousWin": 2003, "followingWin": 2004}
				  ],
				  "max": [
				    {"producer": "Ana", "interval": 4, "previousWin": 2000, "followingWin": 2004}
				  ]
				}
				""";

		mockMvc.perform(get("/api/producers/award-intervals"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json(expected, JsonCompareMode.STRICT));
	}

}
