package com.warehouse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:warehouse_stock_workflow;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.locations=classpath:db/migration,classpath:db/dev",
        "warehouse.security.token-secret=test-token-secret",
        "warehouse.security.ebpf-ingest-key=test-ebpf-key",
        "warehouse.stock.lock.enabled=false"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StockWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminCanPerformStockCheckAndGenerateCheckRecord() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");

        mockMvc.perform(post("/api/stock/checks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"stock-check-1\",\"stockId\":1,\"countedQuantity\":18.00,\"reason\":\"cycle_count\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.resultType").value("CHECK_LOSS"))
                .andExpect(jsonPath("$.data.systemQuantity").value(20.00))
                .andExpect(jsonPath("$.data.countedQuantity").value(18.00))
                .andExpect(jsonPath("$.data.differenceQuantity").value(-2.00));

        mockMvc.perform(get("/api/stock/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(18.00));

        mockMvc.perform(get("/api/stock/checks/page")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].requestId").value("stock-check-1"));
    }

    @Test
    void adminCanReportLossAndOverflow() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");

        mockMvc.perform(post("/api/stock/losses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"stock-loss-1\",\"stockId\":1,\"quantity\":3.00,\"reason\":\"damage\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adjustmentType").value("LOSS"))
                .andExpect(jsonPath("$.data.afterQuantity").value(17.00));

        mockMvc.perform(post("/api/stock/overflows")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"stock-overflow-1\",\"stockId\":1,\"quantity\":2.00,\"reason\":\"unexpected_receipt\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adjustmentType").value("OVERFLOW"))
                .andExpect(jsonPath("$.data.afterQuantity").value(19.00));
    }

    @Test
    void operatorCanSubmitLossApplicationAndAdminApprovalAppliesStockChange() throws Exception {
        String operatorToken = loginAndGetToken("operator", "123456");
        String adminToken = loginAndGetToken("admin", "123456");

        mockMvc.perform(post("/api/stock/losses/apply")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"stock-loss-apply-1\",\"stockId\":3,\"quantity\":4.00,\"reason\":\"damage\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.adjustmentType").value("LOSS"));

        mockMvc.perform(get("/api/stock/3")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(30.00));

        mockMvc.perform(get("/api/stock/adjustments/page")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(post("/api/stock/adjustments/1/approve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true,\"comment\":\"approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.afterQuantity").value(26.00));

        mockMvc.perform(get("/api/stock/3")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(26.00));
    }

    @Test
    void stockCheckApprovalShouldFailWhenStockChangedAfterSubmission() throws Exception {
        String operatorToken = loginAndGetToken("operator", "123456");
        String adminToken = loginAndGetToken("admin", "123456");

        mockMvc.perform(post("/api/stock/checks/apply")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"stock-check-apply-1\",\"stockId\":3,\"countedQuantity\":28.00,\"reason\":\"cycle_count\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        mockMvc.perform(post("/api/stock/adjustments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"stock-adjust-before-approve\",\"stockId\":3,\"quantityChange\":1.00,\"reason\":\"inventory_recount\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.afterQuantity").value(31.00));

        mockMvc.perform(post("/api/stock/checks/1/approve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true,\"comment\":\"approve\"}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value(405));
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
