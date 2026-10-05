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
 * Caso: "and" dentro de um nome não separa produtores; só " and " com espaços
 * dos dois lados separa.
 *
 * Linhas consideradas: as 4 são vencedoras e válidas.
 *   - "Brandon and Roland Emmerich" → Brandon, Roland Emmerich
 *   - "Brandon, Anderson"           → Brandon, Anderson
 *   - "Roland Emmerich"             → Roland Emmerich
 *   - "Anderson"                    → Anderson
 *   Papel de cada nome:
 *   - Brandon: "and" no meio, sem espaços. Uma regex com "and" solto o
 *     cortaria em "Br" e "on".
 *   - Roland Emmerich: "and" seguido de espaço, sem espaço antes. Uma regex
 *     que não exija espaço antes do "and" cortaria a linha 4 em "Rol" e
 *     "Emmerich", e Roland perderia o par 2000 → 2008.
 *   - Anderson: começa com "And" logo depois de ", ". Só seria cortado por uma
 *     regex que ignore maiúsculas e não exija espaço depois do "and"; se ela
 *     também dispensar o espaço antes, o Brandon já falha. Está no max para
 *     que qualquer corte no nome mude o JSON.
 *
 * Vitórias por produtor:
 *   Brandon: 2000, 2003
 *   Roland Emmerich: 2000, 2008
 *   Anderson: 2003, 2011
 *
 * Intervalos:
 *   Brandon: 2000 → 2003 = 3
 *   Roland Emmerich: 2000 → 2008 = 8
 *   Anderson: 2003 → 2011 = 8
 *
 * min = 3 → Brandon (2000)
 * max = 8 → Roland Emmerich (2000) e Anderson (2003), ordenados por previousWin.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/nome-com-and-sem-espacos.csv")
class NomeComAndSemEspacosTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void doesNotSplitAndInsideNames() throws Exception {
		String expected = """
				{
				  "min": [
				    {"producer": "Brandon", "interval": 3, "previousWin": 2000, "followingWin": 2003}
				  ],
				  "max": [
				    {"producer": "Roland Emmerich", "interval": 8, "previousWin": 2000, "followingWin": 2008},
				    {"producer": "Anderson", "interval": 8, "previousWin": 2003, "followingWin": 2011}
				  ]
				}
				""";

		mockMvc.perform(get("/api/producers/award-intervals"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json(expected, JsonCompareMode.STRICT));
	}

}