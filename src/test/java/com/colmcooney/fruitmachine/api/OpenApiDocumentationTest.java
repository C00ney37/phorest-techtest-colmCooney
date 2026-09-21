package com.colmcooney.fruitmachine.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Checks the generated API description covers every endpoint and that Swagger UI is being served. */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void describesEveryEndpointAndItsResponses() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Fruit Machine API"))
                .andExpect(jsonPath("$.paths['/machines'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/machines'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/machines/{machineId}'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/machines/{machineId}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/machines/{machineId}/plays'].post.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/machines/{machineId}/plays'].post.responses['404']").exists());
    }

    @Test
    void marksThePlayCostAndStartingFloatAsRequiredAndTheOtherFieldsAsOptional() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.CreateMachineRequest.required[0]").value("playCost"))
                .andExpect(jsonPath("$.components.schemas.CreateMachineRequest.required[1]").value("startingFloat"))
                .andExpect(jsonPath("$.components.schemas.CreateMachineRequest.required.length()").value(2));
    }

    @Test
    void servesSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
