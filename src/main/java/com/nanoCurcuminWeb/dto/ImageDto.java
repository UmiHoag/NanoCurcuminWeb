package com.nanoCurcuminWeb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
public class ImageDto {
    private Long id;
    private String fileName;
    private String downloadUrl;

}
