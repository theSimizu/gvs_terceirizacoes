package com.h_group.gvs_terceirizacoes;

import com.h_group.gvs_terceirizacoes.config.JwtService;
import com.h_group.gvs_terceirizacoes.entity.*;
import com.h_group.gvs_terceirizacoes.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RoleAuthorizationIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @Autowired private ClientRepository clients;
    @Autowired private ProviderRepository providers;
    @Autowired private ContractRepository contracts;
    @Autowired private PasswordEncoder passwords;
    @Autowired private JwtService jwt;
    @Autowired private ObjectMapper mapper;
    private User admin;
    private User user;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void accounts() {
        admin = account(UserRole.ADM);
        user = account(UserRole.USER);
        adminToken = jwt.generateToken(admin);
        userToken = jwt.generateToken(user);
    }

    @Test
    void loginAndCurrentUserReturnRoleWithoutPassword() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","password":"test-password"}
                        """.formatted(admin.getEmail())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.user.role").value("adm"))
                .andExpect(jsonPath("$.user.password").doesNotExist());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("user"))
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void registrationCannotGrantAdminRole() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        var result = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Attempted admin","email":"%s","password":"test-password","role":"adm"}
                        """.formatted(email))).andReturn();
        int status = result.getResponse().getStatus();
        assertTrue(status == 201 || status == 400);
        if (status == 201) {
            assertEquals(UserRole.USER, users.findByEmailIgnoreCase(email).orElseThrow().getRole());
            assertEquals("user", mapper.readTree(result.getResponse().getContentAsString()).path("role").asString());
        } else {
            assertFalse(users.existsByEmailIgnoreCase(email));
        }
    }

    @Test
    void userCanReadAllExistingResources() throws Exception {
        for (String path : new String[]{"clientes", "prestadores", "contratos", "ordens-servico", "historico"}) {
            mvc.perform(get("/api/" + path).header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void userCannotWriteRegistriesEvenWithValidToken() throws Exception {
        for (String path : new String[]{"clientes", "prestadores", "contratos"}) {
            mvc.perform(post("/api/" + path).header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
            mvc.perform(put("/api/" + path + "/" + UUID.randomUUID()).header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void userCannotDeleteAnyResource() throws Exception {
        for (String path : new String[]{"clientes", "prestadores", "contratos", "ordens-servico"}) {
            mvc.perform(delete("/api/" + path + "/" + UUID.randomUUID()).header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void adminCanMaintainClients() throws Exception {
        var created = mvc.perform(post("/api/clientes").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"legalName":"Role test client","cnpj":"%s","email":"role@example.com","phone":"11999999999"}
                        """.formatted(taxId()))).andExpect(status().isCreated()).andReturn();
        String id = mapper.readTree(created.getResponse().getContentAsString()).path("id").asString();
        mvc.perform(put("/api/clientes/" + id).header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content("{\"legalName\":\"Updated by admin\"}"))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/clientes/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void userCanOperateOrderAndRecordHistoryButOnlyAdminCanDelete() throws Exception {
        Contract contract = contract();
        var created = mvc.perform(post("/api/ordens-servico").header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"contractId":"%s","description":"Authorized user operation","executionDate":"2026-11-02"}
                        """.formatted(contract.getId()))).andExpect(status().isCreated()).andReturn();
        String id = mapper.readTree(created.getResponse().getContentAsString()).path("id").asString();
        mvc.perform(put("/api/ordens-servico/" + id).header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"in_progress\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/historico").header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"action":"create","entity":"ServiceOrder","documentId":"%s","details":{}}
                        """.formatted(id))).andExpect(status().isCreated());
        mvc.perform(delete("/api/ordens-servico/" + id).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/ordens-servico/" + id).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/ordens-servico/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void roleIsRecheckedAfterTokenWasIssued() throws Exception {
        admin.setRole(UserRole.USER);
        users.save(admin);
        mvc.perform(post("/api/clientes").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("user"));
    }

    @Test
    void deletedAccountCannotContinueUsingToken() throws Exception {
        users.delete(user);
        mvc.perform(get("/api/clientes").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forgedSignatureAndUnknownSubjectAreRejected() throws Exception {
        String forged = userToken.substring(0, userToken.lastIndexOf('.') + 1) + "invalid-signature";
        mvc.perform(get("/api/clientes").header("Authorization", "Bearer " + forged))
                .andExpect(status().isUnauthorized());
        user.setId(UUID.randomUUID());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + jwt.generateToken(user)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void contextPathAndPathParametersDoNotBypassAuthentication() throws Exception {
        mvc.perform(get("/gvs/api/clientes").contextPath("/gvs")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/clientes;ignored=value")).andExpect(status().isUnauthorized());
    }

    private User account(UserRole role) {
        User account = new User();
        account.setName("Role test"); account.setEmail(UUID.randomUUID() + "@example.com");
        account.setPassword(passwords.encode("test-password")); account.setRole(role);
        return users.save(account);
    }
    private String taxId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 14);
    }
    private Contract contract() {
        Client client = new Client();
        client.setLegalName("Role test client"); client.setCnpj(taxId());
        client.setEmail("role@example.com"); client.setPhone("11999999999");
        client = clients.save(client);
        Provider provider = new Provider();
        provider.setName("Role test provider"); provider.setTaxId(taxId()); provider.setSpecialty("Cleaning");
        provider.setEmail("provider@example.com"); provider.setPhone("11999999999");
        provider = providers.save(provider);
        Contract contract = new Contract(); contract.setClient(client); contract.setProvider(provider);
        contract.setStartDate(LocalDate.of(2026,11,1)); contract.setAgreedValue(new BigDecimal("100.00"));
        return contracts.save(contract);
    }
}
