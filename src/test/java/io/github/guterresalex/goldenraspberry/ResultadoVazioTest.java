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
 * Caso: nenhum produtor tem 2 ou mais vitórias, então min e max ficam vazios.
 *
 * Linhas consideradas: as 3 são válidas. Só as de 2000 e 2010 são vencedoras.
 * A de 2005 tem winner vazio e não conta como vitória.
 *   - A linha de 2005 existe para que, se um não vencedor fosse contado,
 *     o Produtor A aparecesse com intervalo 5 (2000 → 2005).
 *   - A linha de 2010 existe para que, se os intervalos não fossem separados
 *     por produtor, surgisse um intervalo falso de 10 (A 2000 → B 2010).
 *
 * Vitórias por produtor:
 *   Produtor A: 2000
 *   Produtor B: 2010
 *
 * Intervalos: nenhum, porque cada produtor tem 1 vitória só.
 *
 * min = max = [] → 200 com {"min":[],"max":[]}
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/resultado-vazio.csv")
class ResultadoVazioTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void returnsEmptyListsWhenNoProducerHasTwoWins() throws Exception {
		String expected = """
				{
				  "min": [],
				  "max": []
				}
				""";

		mockMvc.perform(get("/api/producers/award-intervals"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json(expected, JsonCompareMode.STRICT));
	}

}