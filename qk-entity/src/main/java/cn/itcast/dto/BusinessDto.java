package cn.itcast.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDto {
    private Integer businessId;
    private String name;
    private String phone;
    private Integer status;
    private String assignName;
    private Integer page = 1;
    private Integer pageSize = 10;

    private Integer subject;
}
