package cn.itcast.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "ali.oss")
@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AliProperties {
    private String endpoint;
    private String bucketName;
    private String region;
}
