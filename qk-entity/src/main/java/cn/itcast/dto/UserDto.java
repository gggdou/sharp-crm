package cn.itcast.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class UserDto {
    private String name;
    private Integer status;
    private String phone;
    private Integer deptId;
    private Integer page = 1;
    private Integer pageSize = 10;
    private Integer start;
}
