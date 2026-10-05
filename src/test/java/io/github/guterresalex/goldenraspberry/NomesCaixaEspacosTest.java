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
 * Caso: grafias com caixa e espaços diferentes são o mesmo produtor, exibido
 * com a grafia da primeira ocorrência no CSV.
 *
 * Linhas consideradas: as 3 são vencedoras e válidas, com títulos distintos.
 *   - "Ana Souza", "ANA SOUZA" e "  ana   souza  " geram a mesma chave "ana souza".
 *   - A primeira linha do CSV (2010) não é a vitória mais antiga (2000): o nome
 *     exibido vem da ordem no arquivo, não do ano.
 *   - Sem ignorar a caixa, o resultado seria vazio; ignorando a caixa mas sem
 *     colapsar os espaços, min = max = 10 (2000 → 2010).
 *
 * Vitórias por produtor:
 *   Ana Souza: 2000, 2004, 2010
 *
 * Intervalos:
 *   Ana Souza: 2000 → 2004 = 4, 2004 → 2010 = 6
 *
 * min = 4 → Ana Souza (2000)
 * max = 6 → Ana Souza (2004)
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/nomes-caixa-espacos.csv")
class NomesCaixaEspacosTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void mergesProducerNamesIgnoringCaseAndWhitespaceKeepingFirstSpelling() throws Exception {
		String expected = """
				{
				  "min": [
				    {"producer": "Ana Souza", "interval": 4, "previousWin": 2000, "followingWin": 2004}
				  ],
				  "max": [
				    {"producer": "Ana Souza", "interval": 6, "previousWin": 2004, "followingWin": 2010}
				  ]
				}
				""";

		mockMvc.perform(get("/api/producers/award-intervals"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json(expected, JsonCompareMode.STRICT));
	}

}
