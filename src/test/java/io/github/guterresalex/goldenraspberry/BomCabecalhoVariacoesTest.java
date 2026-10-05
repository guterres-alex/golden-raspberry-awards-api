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
 * Caso: um arquivo com BOM (EF BB BF) e cabeçalho " Year ; TITLE;Studios;producers ;WINNER"
 * é aceito: o BOM é removido e cada coluna do cabeçalho é comparada com trim e
 * sem diferenciar maiúsculas.
 *
 * Linhas consideradas: 2 e 3, ambas vencedoras e válidas.
 *   - String.trim() não remove U+FEFF: sem remover o BOM, o cabeçalho é
 *     inválido e o contexto não sobe.
 *   - Sem trim ou com comparação sensível a maiúsculas, o mesmo acontece.
 *   - Se o contexto subisse sem carregar as linhas, o resultado viria vazio.
 *
 * Vitórias por produtor:
 *   Ana: 2000, 2004
 *
 * Intervalos:
 *   Ana: 2000 → 2004 = 4
 *
 * min = max = 4 → Ana (2000)
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/bom-cabecalho-variacoes.csv")
class BomCabecalhoVariacoesTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void acceptsBomAndHeaderWithSpacesAndDifferentCase() throws Exception {
		String expected = """
				{
				  "min": [
				    {"producer": "Ana", "interval": 4, "previousWin": 2000, "followingWin": 2004}
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
