package cn.itcast.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private String name;
    private String subject;
    private String target;
    private Integer page = 1;
    private Integer pageSize = 10;

}
