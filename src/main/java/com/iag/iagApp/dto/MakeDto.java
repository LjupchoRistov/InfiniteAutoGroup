package com.iag.iagApp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MakeDto {
    private Long id;
    private String makeName;
    private String logoUrl;
    private Integer modelCount;
    private Integer offersCount;
}
