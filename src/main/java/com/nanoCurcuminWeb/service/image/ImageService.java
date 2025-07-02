package com.nanoCurcuminWeb.service.image;

import com.nanoCurcuminWeb.dto.ImageDto;
import com.nanoCurcuminWeb.exceptions.ResourceNotFoundException;
import com.nanoCurcuminWeb.model.Image;
import com.nanoCurcuminWeb.model.Product;
import com.nanoCurcuminWeb.repository.ImageRepository;
import com.nanoCurcuminWeb.service.product.IProductService;
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService implements IImageService {
    private final ImageRepository imageRepository;
    private final IProductService productService;
    private final MessageSource messageSource;


    @Override
    public Image getImageById(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("image.not.found", new Object[]{id}, LocaleContextHolder.getLocale())));
    }

    @Override
    public void deleteImageById(Long id) {
        imageRepository.findById(id).ifPresentOrElse(imageRepository::delete, () -> {
            throw new ResourceNotFoundException(messageSource.getMessage("image.not.found", new Object[]{id}, LocaleContextHolder.getLocale()));
        });

    }

    @Override
    public List<ImageDto> saveImages(Long productId, List<MultipartFile> files) {
        Product product = productService.getProductById(productId);

        List<ImageDto> savedImageDto = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                Image image = new Image();
                image.setFileName(file.getOriginalFilename());
                image.setFileType(file.getContentType());
                image.setFileSize(file.getSize());
                image.setProduct(product);

                // Generate unique filename
                String originalFilename = file.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
                
                // Save file to disk (you would implement this based on your file storage strategy)
                String filePath = "/uploads/" + uniqueFilename;
                image.setFilePath(filePath);

                String buildDownloadUrl = "/api/v1/images/image/download/";
                Image savedImage = imageRepository.save(image);

                savedImage.setDownloadUrl(buildDownloadUrl + savedImage.getId());
                imageRepository.save(savedImage);

                ImageDto imageDto = new ImageDto();
                imageDto.setId(savedImage.getId());
                imageDto.setFileName(savedImage.getFileName());
                imageDto.setDownloadUrl(savedImage.getDownloadUrl());
                savedImageDto.add(imageDto);

            } catch (Exception e) {
                throw new RuntimeException("Failed to save image: " + e.getMessage());
            }
        }
        return savedImageDto;
    }

    

    @Override
    public void updateImage(MultipartFile file, Long imageId) {
        Image image = getImageById(imageId);
        try {
            image.setFileName(file.getOriginalFilename());
            image.setFileType(file.getContentType());
            image.setFileSize(file.getSize());
            
            // Generate unique filename for updated image
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // Update file path (you would implement this based on your file storage strategy)
            String filePath = "/uploads/" + uniqueFilename;
            image.setFilePath(filePath);
            
            imageRepository.save(image);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update image: " + e.getMessage());
        }
    }
}
