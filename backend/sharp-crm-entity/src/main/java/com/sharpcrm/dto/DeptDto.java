package com.sharpcrm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeptDto {
    private String name;
    private Integer status;
    private Integer page = 1;
    private Integer pageSize = 10;
}
