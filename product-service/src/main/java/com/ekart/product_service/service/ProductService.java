package com.ekart.product_service.service;

import com.ekart.product_service.dto.ProductRequest;
import com.ekart.product_service.dto.ProductResponse;
import com.ekart.product_service.exceptions.ProductNotFound;
import com.ekart.product_service.model.Product;
import com.ekart.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;  // injecting this dependency using ProductService parameterized constructor


    @CachePut(value = "products", key = "#result.id")
    @CacheEvict(value = "products", key = "'all'")
    public ProductResponse createProduct(ProductRequest productRequest){
        Product product = Product.builder()
                .name(productRequest.name())
                .description(productRequest.description())
                .price(productRequest.price())
                .build();
        productRepository.save(product);
        log.info("Product created successfully");
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice());
    }

    @Cacheable(value = "products", key = "'all'")
    public List<ProductResponse> getAllProducts(){
        return productRepository.findAll()
                .stream()
                .map(product -> new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice()))
                .toList();
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductbyId(String id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFound("Product not found with id: "+id));
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice());
    }

    @Transactional
    @CachePut(value = "products", key = "#id")
    public ProductResponse updateProductById(String id, ProductRequest productRequest){
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFound("Product not found with id: "+id));
        log.info("Product is found to update its value.");
        product.setName(productRequest.name());
        product.setDescription(productRequest.description());
        product.setPrice(productRequest.price());

        log.info("Product value is updated.");

        productRepository.save(product);
        log.info("Product is saved after updation.");
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice());
    }

    @CacheEvict(value = "products", key = "#id")
    public void deleteById(String id){
        if(!productRepository.existsById(id)){
            throw  new ProductNotFound("Product doesn't exist. with id: "+id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted successfully.");
    }

}
