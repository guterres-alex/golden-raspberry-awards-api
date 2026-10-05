package io.github.guterresalex.goldenraspberry;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Caso: verbos e status HTTP (nível 2 de Richardson), com o CSV original.
 *
 *   - POST no endpoint de intervalos → 405 com Problem Details e o cabeçalho
 *     Allow indicando GET.
 *   - GET numa URL inexistente → 404 com Problem Details.
 *
 * O corpo é gerado pelo Spring, e campos como detail e instance variam entre
 * versões. Por isso, e só aqui, a comparação é LENIENT e cobre apenas os
 * campos estáveis do RFC 9457: title e status. O type fica de fora: o Spring
 * omite o campo quando ele vale about:blank, o que o RFC define como equivalente.
 */
@SpringBootTest
@AutoConfigureMockMvc
class VerbosStatusHttpTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void rejectsPostWith405ProblemDetail() throws Exception {
		String expected = """
				{"title": "Method Not Allowed", "status": 405}
				""";

		mockMvc.perform(post("/api/producers/award-intervals"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(header().string(HttpHeaders.ALLOW, containsString("GET")))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(content().json(expected, JsonCompareMode.LENIENT));
	}

	@Test
	void returns404ProblemDetailForUnknownUrl() throws Exception {
		String expected = """
				{"title": "Not Found", "status": 404}
				""";

		mockMvc.perform(get("/api/producers/inexistente"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(content().json(expected, JsonCompareMode.LENIENT));
	}

}
