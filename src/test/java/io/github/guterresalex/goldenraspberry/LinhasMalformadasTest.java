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
 * Caso: linhas malformadas são ignoradas sem derrubar a carga, e o limite de
 * 255 caracteres é testado dos dois lados.
 *
 * Linhas consideradas: 2 e 11. As linhas 3 a 10 são vencedoras da Ana (exceto
 * a 7) entre 2000 e 2010 e são ignoradas:
 *   - Linha 3: 4 colunas. Ler a coluna winner sem checar o tamanho quebraria a carga.
 *   - Linha 4: 6 colunas (";" no fim). Com split(";") sem -1 o vazio final
 *     some e a linha contaria.
 *   - Linha 5: ano "2005a" não é inteiro.
 *   - Linha 6: título vazio após o trim.
 *   - Linha 7: produtores " , " não geram nenhum nome. Não muda o resultado;
 *     prova só que a carga não quebra.
 *   - Linha 8: título com 256 caracteres.
 *   - Linha 9: estúdio com 256 caracteres.
 *   - Linha 10: "Ana, " + produtor com 256 caracteres. A linha inteira é
 *     ignorada; descartar só o nome longo daria a Ana uma vitória em 2008.
 *   - Linha 11: título com exatamente 255 caracteres é válido e conta.
 *   Qualquer linha ignorada que fosse contada dividiria o intervalo da Ana;
 *   ignorar a linha 11 deixaria o resultado vazio.
 *
 * Vitórias por produtor:
 *   Ana: 2000, 2010
 *
 * Intervalos:
 *   Ana: 2000 → 2010 = 10
 *
 * min = max = 10 → Ana (2000)
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/linhas-malformadas.csv")
class LinhasMalformadasTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void ignoresMalformedLines() throws Exception {
		String expected = """
				{
				  "min": [
				    {"producer": "Ana", "interval": 10, "previousWin": 2000, "followingWin": 2010}
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
