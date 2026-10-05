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
 * Caso: três produtores empatam no min e no max; cada lista é ordenada por
 * previousWin e depois por producer, independentemente da ordem no CSV.
 *
 * Linhas consideradas: as 9 são vencedoras e válidas, com títulos distintos.
 *   - A ordem no CSV (Bob, Zed, Amy) define os ids e, portanto, a ordem em que
 *     a query devolve os produtores. Ela difere da ordem esperada:
 *     Bob aparece primeiro mas tem previousWin maior (testa a chave principal);
 *     Zed aparece antes de Amy com o mesmo previousWin (testa o desempate).
 *     Ordenar só por previousWin daria Zed, Amy, Bob; só por nome, Amy, Bob, Zed.
 *
 * Vitórias por produtor:
 *   Bob: 2010, 2011, 2021
 *   Zed: 2000, 2001, 2011
 *   Amy: 2000, 2001, 2011
 *
 * Intervalos:
 *   Bob: 2010 → 2011 = 1, 2011 → 2021 = 10
 *   Zed: 2000 → 2001 = 1, 2001 → 2011 = 10
 *   Amy: 2000 → 2001 = 1, 2001 → 2011 = 10
 *
 * min = 1 → Amy (2000), Zed (2000), Bob (2010)
 * max = 10 → Amy (2001), Zed (2001), Bob (2011)
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/empates-min-max.csv")
class EmpatesMinMaxTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void includesAllTiedProducersSortedByPreviousWinThenProducer() throws Exception {
		String expected = """
				{
				  "min": [
				    {"producer": "Amy", "interval": 1, "previousWin": 2000, "followingWin": 2001},
				    {"producer": "Zed", "interval": 1, "previousWin": 2000, "followingWin": 2001},
				    {"producer": "Bob", "interval": 1, "previousWin": 2010, "followingWin": 2011}
				  ],
				  "max": [
				    {"producer": "Amy", "interval": 10, "previousWin": 2001, "followingWin": 2011},
				    {"producer": "Zed", "interval": 10, "previousWin": 2001, "followingWin": 2011},
				    {"producer": "Bob", "interval": 10, "previousWin": 2011, "followingWin": 2021}
				  ]
				}
				""";

		mockMvc.perform(get("/api/producers/award-intervals"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json(expected, JsonCompareMode.STRICT));
	}

}
