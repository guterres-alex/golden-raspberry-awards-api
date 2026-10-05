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
 * Caso: um filme repetido (mesmo título sem diferenciar caixa + mesmo ano) é
 * ignorado e vale a primeira ocorrência.
 *
 * Linhas consideradas: 2, 3, 5 e 6.
 *   - Linha 4 (2005, "FILME 2", Bia) repete a linha 3 e é ignorada.
 *   - Linha 6 (2009, "Filme 1") tem o título da linha 2 em outro ano: não é
 *     repetição e conta.
 *   - Linha 5 (2006, Bia) existe para que a repetição, se contada, mude o min.
 *   - Repetição contada ou comparada com caixa: min = 1 (Bia).
 *     Valendo a última ocorrência: min = 1 (Bia).
 *     Repetição só pelo título, sem o ano: min = max = 5.
 *
 * Vitórias por produtor:
 *   Ana: 2000, 2005, 2009
 *   Bia: 2006 (uma vitória, fora do cálculo)
 *
 * Intervalos:
 *   Ana: 2000 → 2005 = 5, 2005 → 2009 = 4
 *
 * min = 4 → Ana (2005)
 * max = 5 → Ana (2000)
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/filme-repetido.csv")
class FilmeRepetidoTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void ignoresRepeatedMovieKeepingFirstOccurrence() throws Exception {
		String expected = """
				{
				  "min": [
				    {"producer": "Ana", "interval": 4, "previousWin": 2005, "followingWin": 2009}
				  ],
				  "max": [
				    {"producer": "Ana", "interval": 5, "previousWin": 2000, "followingWin": 2005}
				  ]
				}
				""";

		mockMvc.perform(get("/api/producers/award-intervals"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json(expected, JsonCompareMode.STRICT));
	}

}
