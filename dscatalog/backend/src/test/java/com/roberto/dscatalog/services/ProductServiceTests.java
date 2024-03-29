package com.roberto.dscatalog.services;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import com.roberto.dscatalog.dto.ProductDTO;
import com.roberto.dscatalog.entities.Category;
import com.roberto.dscatalog.entities.Product;
import com.roberto.dscatalog.repositories.CategoryRepository;
import com.roberto.dscatalog.repositories.ProductRepository;
import com.roberto.dscatalog.services.exceptions.DataBaseException;
import com.roberto.dscatalog.services.exceptions.ResourceNotFoundException;
import com.roberto.dscatalog.tests.Factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {
 
    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;

    @Mock
    private CategoryRepository categoryRepository;

    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private PageImpl<Product> page;
    private Product product;
    private Category category;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 4L;

        //Product instance and list
        product = Factory.createProduct();
        page = new PageImpl<>(List.of(product));
        category = Factory.createCategory();
        productDTO = Factory.createProductDTO();

        //mockite configuration
        //functions with objects/page return
        Mockito.when(repository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);
        Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
        Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());
        Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(product);
        
        Mockito.when(repository.getOne(existingId)).thenReturn(product);
        Mockito.when(repository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);

        Mockito.when(categoryRepository.getOne(existingId)).thenReturn(category);
        Mockito.when(categoryRepository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);

        //funtions without return
        Mockito.doNothing().when(repository).deleteById(existingId);

        //funtions throws return
        Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExistingId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
    }

    @Test
    public void updateShouldReturnResourceNotFoundExceptionIdNotExists() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.update(nonExistingId, productDTO);
        });
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExisting() {
        
        ProductDTO result = service.update(existingId, productDTO);
        Assertions.assertNotNull(result);
    }

    @Test
    public void findByIdShouldReturnResourceNotFoundExceptionWhenIdNotExists() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(nonExistingId);
        });
        Mockito.verify(repository).findById(nonExistingId);
    }

    @Test
    public void findByIdShouldReturnProductWhenIdExists() {
        ProductDTO result = service.findById(existingId);
        Assertions.assertNotNull(result);
        Mockito.verify(repository).findById(existingId);
    }

    @Test
    public void findAllPagedShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductDTO> result = service.findAllPaged(pageable);
        Assertions.assertNotNull(result);
        Mockito.verify(repository).findAll(pageable);
    }

    @Test
    public void deleteShouldThrowDataBaseExceptionDoesNotExists() {
        
        Assertions.assertThrows(DataBaseException.class, () -> {
            service.delete(dependentId);
        });

        Mockito.verify(repository, Mockito.times(1)).deleteById(dependentId);
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionDoesNotExists() {
        
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(nonExistingId);
        });

        Mockito.verify(repository, Mockito.times(1)).deleteById(nonExistingId);
    }

    @Test
    public void deleteShouldDoNotThingWhenIdExists() {
        
        Assertions.assertDoesNotThrow(() -> {
            service.delete(existingId);
        });

        Mockito.verify(repository, Mockito.times(1)).deleteById(existingId);
    }
}
