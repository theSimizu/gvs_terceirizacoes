package com.h_group.gvs_terceirizacoes;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ResourceFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeAll
    void authenticate() throws Exception {
        String email = "resource-suite-" + UUID.randomUUID() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Resource Suite",
                                  "email": "%s",
                                  "password": "secret123"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "secret123"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        token = read(result, "token");
    }

    @Test
    void providerContractServiceOrderAndHistoryFlow() throws Exception {
        MvcResult providerResult = mockMvc.perform(post("/api/prestadores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Provider Name",
                                  "taxId": "%s",
                                  "specialty": "Electrical",
                                  "email": "provider@example.com",
                                  "phone": "(11) 98888-7777",
                                  "available": true
                                }
                                """.formatted(uniqueTaxId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.available").value(true))
                .andReturn();

        String providerId = read(providerResult, "id");

        MvcResult clientResult = mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "legalName": "Contract Client LTDA",
                                  "cnpj": "%s",
                                  "email": "client@example.com",
                                  "phone": "(11) 99999-8888"
                                }
                                """.formatted(uniqueCnpj())))
                .andExpect(status().isCreated())
                .andReturn();

        String clientId = read(clientResult, "id");

        MvcResult contractResult = mockMvc.perform(post("/api/contratos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": "%s",
                                  "providerId": "%s",
                                  "startDate": "2026-01-01",
                                  "endDate": "2026-12-31",
                                  "agreedValue": 5000.00,
                                  "status": "active"
                                }
                                """.formatted(clientId, providerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.client.legalName").value("Contract Client LTDA"))
                .andExpect(jsonPath("$.providerId").value(providerId))
                .andExpect(jsonPath("$.provider.name").value("Provider Name"))
                .andExpect(jsonPath("$.agreedValue").value(5000.00))
                .andReturn();

        String contractId = read(contractResult, "id");

        mockMvc.perform(post("/api/ordens-servico")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contractId": "%s",
                                  "description": "Preventive maintenance",
                                  "executionDate": "2026-02-01",
                                  "status": "pending"
                                }
                                """.formatted(contractId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contractId").value(contractId))
                .andExpect(jsonPath("$.contract.id").value(contractId))
                .andExpect(jsonPath("$.status").value("pending"));

        mockMvc.perform(post("/api/historico")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "Created contract",
                                  "entity": "Contract",
                                  "documentId": "%s",
                                  "details": {
                                    "source": "integration-test",
                                    "count": 1
                                  }
                                }
                                """.formatted(contractId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.details.source").value("integration-test"))
                .andExpect(jsonPath("$.date").exists());

        mockMvc.perform(get("/api/historico")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").exists());
    }

    @Test
    void openApiDocumentationIsPublic() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("GVS Terceirizacoes API"));
    }

    @Test
    void swaggerUiIsPublic() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    private String read(MvcResult result, String field) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get(field)
                .asString();
    }

    private String uniqueTaxId() {
        return "TAX" + UUID.randomUUID().toString().replace("-", "").substring(0, 13);
    }

    private String uniqueCnpj() {
        return "CNPJ" + UUID.randomUUID().toString().replace("-", "").substring(0, 13);
    }
}
