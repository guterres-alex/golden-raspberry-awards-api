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
 * Caso: winner conta como vitória só se, após o trim, for "yes" sem
 * diferenciar maiúsculas.
 *
 * Linhas consideradas: todas as 6 são válidas.
 *   - Vencedoras: linha 2 ("YES"), linha 4 ("Yes") e linha 7 (" yes ").
 *   - Não vencedoras: linha 3 ("y"), linha 5 ("true") e linha 6 ("no").
 *   - Cada não vencedora fica entre duas vitórias reais da Ana:
 *     "y" contado → max = 5; "true" ou "no" contado → min = 1.
 *   - Cada vencedora muda o resultado se não for contada:
 *     sem "YES" → min = max = 3; sem "Yes" → min = max = 13;
 *     sem trim em " yes " → min = max = 10.
 *
 * Vitórias por produtor:
 *   Ana: 2000, 2010, 2013
 *
 * Intervalos:
 *   Ana: 2000 → 2010 = 10, 2010 → 2013 = 3
 *
 * min = 3 → Ana (2010)
 * max = 10 → Ana (2000)
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/winner-variacoes.csv")
class WinnerVariacoesTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void countsOnlyYesIgnoringCaseAndSurroundingSpaces() throws Exception {
		String expected = """
				{
				  "min": [
				    {"producer": "Ana", "interval": 3, "previousWin": 2010, "followingWin": 2013}
				  ],
				  "max": [
				    {"producer": "Ana", "interval": 10, "previousWin": 2000, "followingWin": 2010}
				  ]
				}
				""";

		mockMvc.perform(get("/api/producers/award-intervals"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json(expected, JsonCompareMode.STRICT));
	}

}
