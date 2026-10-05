package io.github.guterresalex.goldenraspberry;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AwardIntervalsOriginalCsvTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void returnsMinAndMaxIntervalsFromOriginalCsv() throws Exception {
		String expected = """
				{
				  "min": [
				    {"producer": "Joel Silver", "interval": 1, "previousWin": 1990, "followingWin": 1991}
				  ],
				  "max": [
				    {"producer": "Matthew Vaughn", "interval": 13, "previousWin": 2002, "followingWin": 2015}
				  ]
				}
				""";

		mockMvc.perform(get("/api/producers/award-intervals"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json(expected, JsonCompareMode.STRICT));
	}

}
