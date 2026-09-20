package com.example.streakup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StreakUpApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void contextLoads() {
	}

	@Test
	void registrationLoginAndActivityFlowDoesNotExposePasswordHash() throws Exception {
		String registration = """
				{"username":"runner","email":"runner@example.com","password":"password123"}
				""";
		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(registration))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());

		String login = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"runner@example.com","password":"password123"}
								"""))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		JsonNode token = objectMapper.readTree(login).get("token");

		mockMvc.perform(post("/api/activities")
						.header("Authorization", "Bearer " + token.asText())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"type":"running","date":"2026-09-19","durationMinutes":30}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.passwordHash").doesNotExist())
				.andExpect(jsonPath("$.type").value("RUNNING"));
	}
}
