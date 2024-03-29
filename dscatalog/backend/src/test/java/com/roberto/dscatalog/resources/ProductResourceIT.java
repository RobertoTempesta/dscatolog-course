package com.roberto.dscatalog.resources;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roberto.dscatalog.dto.ProductDTO;
import com.roberto.dscatalog.tests.Factory;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductResourceIT {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long existingId;
    private long nonExistingId;
    private long countTotalProducts;
    
    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        countTotalProducts = 25L;
    }

    @Test
    public void updateShouldReturnNotFoundExceptionWhenNonExistinfId() throws Exception {
        ProductDTO productDTO = Factory.createProductDTO();
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc.perform(get("/products/{id}", nonExistingId)
            .content(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));
        
        result.andExpect(status().isNotFound());
    }

    @Test
    public void updateShouldReturnProductDTOWhenExistinfId() throws Exception {
        ProductDTO productDTO = Factory.createProductDTO();
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        String expectedName = productDTO.getName();

        ResultActions result = mockMvc.perform(put("/products/{id}", existingId)
            .content(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));
        
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.id").value(existingId));
        result.andExpect(jsonPath("$.name").value(expectedName));
        //result.andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void findAllShouldReturnPageWhenSortByName() throws Exception {
        ResultActions result = mockMvc.perform(get("/products?page=0&size=10&sort=name,asc")
            .accept(MediaType.APPLICATION_JSON));
        
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.totalElements").value(countTotalProducts));
        result.andExpect(jsonPath("$.content").exists());
        result.andExpect(jsonPath("$.content[0].name").value("Macbook Pro"));
        result.andExpect(jsonPath("$.content[1].name").value("PC Gamer"));
        result.andExpect(jsonPath("$.content[2].name").value("PC Gamer Alfa"));
    }
}
