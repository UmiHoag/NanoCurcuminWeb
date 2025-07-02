package com.nanoCurcuminWeb.service.product;

import com.nanoCurcuminWeb.dto.ImageDto;
import com.nanoCurcuminWeb.dto.ProductDto;
import com.nanoCurcuminWeb.exceptions.AlreadyExistsException;
import com.nanoCurcuminWeb.exceptions.ResourceNotFoundException;
import com.nanoCurcuminWeb.model.*;
import com.nanoCurcuminWeb.repository.*;
import com.nanoCurcuminWeb.request.AddProductRequest;
import com.nanoCurcuminWeb.request.ProductUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final MessageSource messageSource;
    private final ImageRepository imageRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public Product addProduct(AddProductRequest request) {
        if (productExists(request.getName(), request.getBrand())) {
                throw new AlreadyExistsException(messageSource.getMessage("product.already.exists", new Object[]{request.getBrand(), request.getName()}, LocaleContextHolder.getLocale()));
        }
        Category category = Optional.ofNullable(categoryRepository.findByName(request.getCategory().getName()))
                .orElseGet(() -> {
                    Category newCategory = new Category(request.getCategory().getName());
                    return categoryRepository.save(newCategory);
                });
        request.setCategory(category);
        return productRepository.save(createProduct(request, category));
    }

    private boolean productExists(String name, String brand) {
        return productRepository.existsByNameAndBrand(name, brand);
    }

    private Product createProduct(AddProductRequest request, Category category) {
        return new Product(
                request.getName(),
                request.getBrand(),
                request.getPrice(),
                request.getInventory(),
                request.getDescription(),
                category
        );
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("product.not.found", null, LocaleContextHolder.getLocale())));
    }

    @Override
    public void deleteProductById(Long id) {
        List<CartItem> cartItems = cartItemRepository.findByProductId(id);
        List<OrderItem> orderItems = orderItemRepository.findByProductId(id);
        productRepository.findById(id)
                .ifPresentOrElse(product -> {
                    // Functional approach for category removal
                    Optional.ofNullable(product.getCategory())
                            .ifPresent(category -> category.getProducts().remove(product));
                    product.setCategory(null);

                    // Functional approach for updating cart items
                    cartItems.stream()
                            .peek(cartItem -> cartItem.setProduct(null))
                            .peek(CartItem::setTotalPrice)
                            .forEach(cartItemRepository::save);

                    // Functional approach for updating order items
                    orderItems.stream()
                            .peek(orderItem -> orderItem.setProduct(null))
                            .forEach(orderItemRepository::save);

                    productRepository.delete(product);
                }, () -> {
                    throw new EntityNotFoundException(messageSource.getMessage("product.not.found", null, LocaleContextHolder.getLocale()));
                });
    }

    @Override
    public Product updateProduct(ProductUpdateRequest request, Long productId) {
        return productRepository.findById(productId)
                .map(existingProduct -> updateExistingProduct(existingProduct, request))
                .map(productRepository::save)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("product.not.found", null, LocaleContextHolder.getLocale())));
    }

    private Product updateExistingProduct(Product existingProduct, ProductUpdateRequest request) {
        existingProduct.setName(request.getName());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setInventory(request.getInventory());
        existingProduct.setDescription(request.getDescription());

        Category category = categoryRepository.findByName(request.getCategory().getName());
        existingProduct.setCategory(category);
        return existingProduct;

    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryName(category);
    }

    @Override
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    @Override
    public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
        return productRepository.findByCategoryNameAndBrand(category, brand);
    }

    @Override
    public List<Product> getProductsByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public List<Product> getProductsByBrandAndName(String brand, String name) {
        return productRepository.findByBrandAndName(brand, name);
    }

    @Override
    public Long countProductsByBrandAndName(String brand, String name) {
        return productRepository.countByBrandAndName(brand, name);
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products) {
        return products.stream().map(this::convertToDto).toList();
    }

    @Override
    public ProductDto convertToDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);
        List<Image> images = imageRepository.findByProductId(product.getId());
        List<ImageDto> imageDtos = images.stream()
                .map(image -> modelMapper.map(image, ImageDto.class))
                .toList();
        productDto.setImages(imageDtos);
        return productDto;
    }

    @Override
    public List<Product> findDistinctProductsByName() {
        List<Product> products = productRepository.findAll();
        Map<String, Product> distinctProductsMap = products.stream()
                .collect(Collectors.toMap(
                        Product::getName,
                        product -> product,
                        (existing, replacement) -> existing));
        return new ArrayList<>(distinctProductsMap.values());
    }

    @Override
    public List<String> getAllDistinctBrands() {
        return productRepository.findAll().stream()
                .map(Product::getBrand)
                .distinct()
                .collect(Collectors.toList());
    }

}
