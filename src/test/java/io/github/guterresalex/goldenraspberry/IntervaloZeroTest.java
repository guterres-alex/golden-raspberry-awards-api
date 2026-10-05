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
 * Caso: o mesmo produtor vence com dois filmes diferentes no mesmo ano,
 * e o intervalo 0 é válido.
 *
 * Linhas consideradas: as 4 são vencedoras e válidas. Filme 1 e Filme 2 têm
 * títulos diferentes, então não são filme repetido.
 *   - O Produtor B existe para que min e max sejam diferentes: se o intervalo 0
 *     fosse descartado, ou se os anos de vitória fossem deduplicados, o min
 *     viraria o Produtor B com 3.
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
@TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/intervalo-zero.csv")
class IntervaloZeroTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void countsSameYearWinsAsZeroInterval() throws Exception {
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
