package com.ekart.product_service.service;

import com.ekart.product_service.dto.ProductRequest;
import com.ekart.product_service.dto.ProductResponse;
import com.ekart.product_service.model.Product;
import com.ekart.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;  // injecting this dependency using ProductService parameterized constructor

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

    public List<ProductResponse> getAllProducts(){
        return productRepository.findAll()
                .stream()
                .map(product -> new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice()))
                .toList();
    }

    public ProductResponse getProductbyId(String id) {
        Product product = productRepository.findById(id).orElseThrow();
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice());
    }

    @Transactional
    public ProductResponse updateProductById(String id, ProductRequest productRequest){
        Product product = productRepository.findById(id).orElseThrow();
        log.info("Product is found to update its value.");
        product.setName(productRequest.name());
        product.setDescription(productRequest.description());
        product.setPrice(productRequest.price());

        log.info("Product value is updated.");

        productRepository.save(product);
        log.info("Product is saved after updation.");
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice());
    }

    public void deleteById(String id){
        if(!productRepository.existsById(id)){
            throw new RuntimeException("Product doesn't exist.");
        }
        productRepository.deleteById(id);
        log.info("Product deleted successfully.");
    }

}
