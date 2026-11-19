package com.sharpcrm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperateLogDto {
    private String operateUserName;
    private Integer page = 1;
    private Integer pageSize = 10;
}
