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
 * Caso: produtor e estúdio com exatamente 255 caracteres são válidos e contam
 * no resultado. O lado de 256 está em LinhasMalformadasTest.
 *
 * P₂₅₅ = "P" repetido 255 vezes; S₂₅₅ = "S" repetido 255 vezes.
 *
 * Linhas consideradas: as 2 são vencedoras e válidas.
 *   - Linha 2: produtor P₂₅₅.
 *   - Linha 3: estúdio S₂₅₅ e produtor P₂₅₅.
 *   - Um off-by-one no limite do produtor ignoraria as duas linhas; no do
 *     estúdio, ignoraria a linha 3 e deixaria P₂₅₅ com uma só vitória. Nos
 *     dois casos o resultado ficaria vazio. Uma coluna menor que 255 no banco
 *     impediria a carga.
 *
 * Vitórias por produtor:
 *   P₂₅₅: 2000, 2005
 *
 * Intervalos:
 *   P₂₅₅: 2000 → 2005 = 5
 *
 * min = max = 5 → P₂₅₅ (2000), com o nome completo de 255 caracteres.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/limite-255-produtor-estudio.csv")
class Limite255ProdutorEstudioTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void acceptsProducerAndStudioWith255Characters() throws Exception {
		String expected = """
				{
				  "min": [
				    {"producer": "%1$s", "interval": 5, "previousWin": 2000, "followingWin": 2005}
				  ],
				  "max": [
				    {"producer": "%1$s", "interval": 5, "previousWin": 2000, "followingWin": 2005}
				  ]
				}
				""".formatted("P".repeat(255));

		mockMvc.perform(get("/api/producers/award-intervals"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json(expected, JsonCompareMode.STRICT));
	}

}
