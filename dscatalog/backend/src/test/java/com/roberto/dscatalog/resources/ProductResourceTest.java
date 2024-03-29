package com.roberto.dscatalog.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roberto.dscatalog.dto.ProductDTO;
import com.roberto.dscatalog.services.ProductService;
import com.roberto.dscatalog.services.exceptions.ResourceNotFoundException;
import com.roberto.dscatalog.tests.Factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ProductResource.class)
public class ProductResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService service;

    @Autowired
    private ObjectMapper objectMapper;

    private PageImpl<ProductDTO> page;
    private ProductDTO productDTO;
    private long existingId;
    private long nonExistingId;
    private long dependentId;
    
    @BeforeEach
    void setUp() throws Exception {
        productDTO = Factory.createProductDTO();
        page = new PageImpl<>(List.of(productDTO));
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;

        when(service.findAllPaged(any())).thenReturn(page);
        when(service.findById(existingId)).thenReturn(productDTO);
        when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);

        when(service.insert(any())).thenReturn(productDTO);
        when(service.update(eq(existingId), any())).thenReturn(productDTO);
        when(service.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);

        doNothing().when(service).delete(existingId);
        doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);
        doThrow(DataIntegrityViolationException.class).when(service).delete(dependentId);
    }

    @Test
    public void deleteShouldReturnNotFoundWhenNonExistingId() throws Exception {
        ResultActions result = mockMvc.perform(delete("/products/{id}", nonExistingId)
            .accept(MediaType.APPLICATION_JSON));
        
        result.andExpect(status().isNotFound());
    }

    @Test
    public void deleteShouldReturnNoContentWhenExistingId() throws Exception {
        ResultActions result = mockMvc.perform(delete("/products/{id}", existingId)
            .accept(MediaType.APPLICATION_JSON));
        
        result.andExpect(status().isNoContent());
    }

    @Test
    public void insertShouldReturnProductDTO() throws Exception {
        ProductDTO newDTO = productDTO;
        newDTO.setId(null);
        String jsonBody = objectMapper.writeValueAsString(newDTO);

        ResultActions result = mockMvc.perform(post("/products")
            .content(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));
        
        result.andExpect(status().isCreated());
    }

    @Test
    public void updateShouldReturnProductDTOWhenExistinfId() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc.perform(put("/products/{id}", existingId)
            .content(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));
        
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.id").exists());
        result.andExpect(jsonPath("$.name").exists());
        result.andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void updateShouldReturnNotFoundExceptionWhenNonExistinfId() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc.perform(get("/products/{id}", nonExistingId)
            .content(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));
        
        result.andExpect(status().isNotFound());
    }

    @Test
    public void findbyIdShouldReturnNotFoundExceptionWhenNonExistinfId() throws Exception {
        ResultActions result = mockMvc.perform(get("/products/{id}", nonExistingId)
            .accept(MediaType.APPLICATION_JSON));
        
        result.andExpect(status().isNotFound());
    }

    @Test
    public void findbyIdShouldReturnProductDTOWhenExistinfId() throws Exception {
        ResultActions result = mockMvc.perform(get("/products/{id}", existingId)
            .accept(MediaType.APPLICATION_JSON));
        
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.id").exists());
        result.andExpect(jsonPath("$.name").exists());
        result.andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void findAllShouldReturnPage() throws Exception {
        ResultActions result = mockMvc.perform(get("/products")
            .accept(MediaType.APPLICATION_JSON));
        
        result.andExpect(status().isOk());
    }
}
