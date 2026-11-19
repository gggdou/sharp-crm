package com.sharpcrm.common;

import cn.hutool.core.io.IoUtil;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云 OSS 文件上传工具
 * 凭证从环境变量 OSS_ACCESS_KEY_ID / OSS_ACCESS_KEY_SECRET 读取
 */
@Component
public class OSSUtils {

    @Autowired
    private AliProperties ossUtils;

    /**
     * 将输入流上传到 OSS，返回文件访问 URL
     *
     * @param inputStream 待上传的文件流
     * @param fileName    原始文件名
     * @return 文件访问 URL
     */
    public String upload(InputStream inputStream, String fileName) throws Exception {
        String endpoint = ossUtils.getEndpoint();
        String bucketName = ossUtils.getBucketName();
        String region = ossUtils.getRegion();

        EnvironmentVariableCredentialsProvider credentialsProvider =
                CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        String objectName = UUID.randomUUID().toString() + "_" + fileName;

        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            byte[] bytes = IoUtil.readBytes(inputStream);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            PutObjectRequest request = new PutObjectRequest(
                    bucketName, objectName, new ByteArrayInputStream(bytes), metadata);
            ossClient.putObject(request);
            return "https://" + bucketName + "." + endpoint.substring("https://".length()) + "/" + objectName;
        } catch (OSSException oe) {
            throw new RuntimeException("OSS 上传失败: " + oe.getErrorMessage(), oe);
        } catch (ClientException ce) {
            throw new RuntimeException("OSS 客户端异常: " + ce.getMessage(), ce);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 上传 SpringMVC 上传的文件
     */
    public String upload(MultipartFile file) throws Exception {
        return upload(file.getInputStream(), file.getOriginalFilename());
    }

    /**
     * 上传本地文件
     */
    public String upload(File file, String fileName) throws Exception {
        return upload(new FileInputStream(file), fileName);
    }
}
