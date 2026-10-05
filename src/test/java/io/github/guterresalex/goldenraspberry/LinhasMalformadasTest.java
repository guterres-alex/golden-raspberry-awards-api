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
 * Linhas consideradas: 2, 8 e 12. As linhas 3 a 7 e 9 a 11 são ignoradas:
 *   - Linha 3: 4 colunas, sem a coluna winner. Ler a coluna winner sem checar
 *     o tamanho quebraria a carga.
 *   - Linha 4: 6 colunas (";" no fim). Com split(";") sem -1 o vazio final
 *     some e a linha contaria.
 *   - Linha 5: ano "2005a" não é inteiro.
 *   - Linha 6: título vazio após o trim.
 *   - Linha 7: produtores " , " não geram nenhum nome. Se fosse aceita, ela
 *     ocuparia a chave ("Filme 6", 2006) e a linha 8 seria descartada como
 *     filme repetido, deixando só 2000 → 2010.
 *   - Linha 8: mesmo título e ano da linha 7, com a Ana. É válida e conta.
 *   - Linha 9: título com 256 caracteres.
 *   - Linha 10: estúdio com 256 caracteres.
 *   - Linha 11: "Ana, " + produtor com 256 caracteres. A linha inteira é
 *     ignorada; descartar só o nome longo daria a Ana uma vitória em 2008.
 *   - Linha 12: título com exatamente 255 caracteres é válido e conta.
 *   As linhas 4, 6, 9, 10 e 11, se contadas, dariam à Ana uma vitória a mais
 *   e mudariam o min; as linhas 3 e 5 quebrariam a carga sem a validação;
 *   ignorar a linha 12 deixaria min = max = 6.
 *
 * Vitórias por produtor:
 *   Ana: 2000, 2006, 2010
 *
 * Intervalos:
 *   Ana: 2000 → 2006 = 6
 *   Ana: 2006 → 2010 = 4
 *
 * min = 4 → Ana (2006)
 * max = 6 → Ana (2000)
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
				    {"producer": "Ana", "interval": 4, "previousWin": 2006, "followingWin": 2010}
				  ],
				  "max": [
				    {"producer": "Ana", "interval": 6, "previousWin": 2000, "followingWin": 2006}
				  ]
				}
				""";

		mockMvc.perform(get("/api/producers/award-intervals"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json(expected, JsonCompareMode.STRICT));
	}

}
