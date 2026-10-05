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
 * Caso: um mesmo produtor tem dois pares empatados no min e dois no max, e
 * aparece nas duas listas.
 *
 * Linhas consideradas: as 5 são vencedoras e válidas, com títulos distintos.
 *   - A ordem no CSV (2015, 2002, 2008, 2001, 2014) não é cronológica: se o
 *     ano sair do order by da query, os intervalos tendem a virar -13, 6, -7, 13
 *     (o H2 não garante a ordem de inserção).
 *
 * Vitórias por produtor:
 *   Produtor A: 2001, 2002, 2008, 2014, 2015
 *
 * Intervalos:
 *   Produtor A: 2001 → 2002 = 1, 2002 → 2008 = 6, 2008 → 2014 = 6, 2014 → 2015 = 1
 *
 * min = 1 → Produtor A (2001), Produtor A (2014)
 * max = 6 → Produtor A (2002), Produtor A (2008)
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/varios-pares-mesmo-produtor.csv")
class VariosParesMesmoProdutorTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void includesEveryTiedPairOfTheSameProducerInBothLists() throws Exception {
		String expected = """
				{
				  "min": [
				    {"producer": "Produtor A", "interval": 1, "previousWin": 2001, "followingWin": 2002},
				    {"producer": "Produtor A", "interval": 1, "previousWin": 2014, "followingWin": 2015}
				  ],
				  "max": [
				    {"producer": "Produtor A", "interval": 6, "previousWin": 2002, "followingWin": 2008},
				    {"producer": "Produtor A", "interval": 6, "previousWin": 2008, "followingWin": 2014}
				  ]
				}
				""";

		mockMvc.perform(get("/api/producers/award-intervals"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json(expected, JsonCompareMode.STRICT));
	}

}
