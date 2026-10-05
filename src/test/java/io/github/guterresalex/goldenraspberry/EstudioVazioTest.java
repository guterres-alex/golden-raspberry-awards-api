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
 * Caso: filme sem estúdio é válido e conta no resultado.
 *
 * Linhas consideradas: as 3 são vencedoras e válidas.
 *   - Linha 2: campo studios vazio (";;").
 *   - Linha 4: studios " , ", que após o split e o trim não gera nenhum nome.
 *     No campo producers isso torna a linha malformada; em studios, não.
 *   - Uma validação de "nenhum estúdio" mudaria o resultado: sem a linha 2,
 *     min = max = 2; sem a linha 4, min = max = 4; sem as duas, vazio.
 *
 * Vitórias por produtor:
 *   Produtor A: 2000, 2004, 2006
 *
 * Intervalos:
 *   Produtor A: 2000 → 2004 = 4
 *   Produtor A: 2004 → 2006 = 2
 *
 * min = 2 → Produtor A (2004)
 * max = 4 → Produtor A (2000)
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/estudio-vazio.csv")
class EstudioVazioTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void countsMoviesWithoutStudios() throws Exception {
		String expected = """
				{
				  "min": [
				    {"producer": "Produtor A", "interval": 2, "previousWin": 2004, "followingWin": 2006}
				  ],
				  "max": [
				    {"producer": "Produtor A", "interval": 4, "previousWin": 2000, "followingWin": 2004}
				  ]
				}
				""";

		mockMvc.perform(get("/api/producers/award-intervals"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json(expected, JsonCompareMode.STRICT));
	}

}
