package com.warehouse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:warehouse_security;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.locations=classpath:db/migration,classpath:db/dev",
        "warehouse.security.token-secret=test-token-secret",
        "warehouse.security.ebpf-ingest-key=test-ebpf-key",
        "warehouse.stock.lock.enabled=false"
})
@AutoConfigureMockMvc
class SecurityFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void operatorCannotOpenAdminDashboard() throws Exception {
        String token = loginAndGetToken("operator", "123456");

        mockMvc.perform(get("/api/security/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void adminCanIngestAndViewEbpfEvents() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");

        mockMvc.perform(post("/api/security/ebpf/ingest")
                        .header("X-EBPF-KEY", "test-ebpf-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventType\":\"EXEC\",\"severity\":\"HIGH\",\"processId\":9527,\"processName\":\"java\",\"syscallName\":\"execve\",\"targetPath\":\"/bin/sh\",\"summary\":\"suspicious shell spawn\",\"detail\":\"integration test event\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/security/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalEvents").value(1))
                .andExpect(jsonPath("$.data.openAlerts").value(1));
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.path("data").path("token").asText();
    }
}
