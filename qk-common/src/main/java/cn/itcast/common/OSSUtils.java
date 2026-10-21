package cn.itcast.common;
import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

@Component
public class OSSUtils {

    // 依赖注入
    @Autowired
    private AliProperties ossUtils;




//    @Value("${ali.oss.endpoint}")
//    private String endpoint;
//    @Value("${ali.oss.bucketName}")
//    private String bucketName;
//    @Value("${ali.oss.region}")
//    private String region;


    public String upload(InputStream inputStream, String fileName) throws Exception {

         String endpoint = ossUtils.getEndpoint(); // 从配置文件中获取endpoint
         String bucketName = ossUtils.getBucketName();
         String region = ossUtils.getRegion();

        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
//        String endpoint = "https://oss-cn-beijing.aliyuncs.com";
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        // 填写Bucket名称，例如examplebucket。
//        String bucketName = "heima-test-gyj";
        // 填写PutObjectRequest的key，即Object完整名称。完整名称不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = UUID.randomUUID().toString() + "_" + fileName;
        // 填写Bucket所在地域。以华东1（杭州）为例，Region填写为cn-hangzhou。
//        String region = "cn-beijing";

        // 创建OSSClient实例。
        // 当OSSClient实例不再使用时，调用shutdown方法以释放资源。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, new File("D:\\Pictures\\nl.png"));

            // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
            // ObjectMetadata metadata = new ObjectMetadata();
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            // metadata.setObjectAcl(CannedAccessControlList.Private);
            // putObjectRequest.setMetadata(metadata);

            // 上传字符串。
            PutObjectResult result = ossClient.putObject(putObjectRequest);

            String url = "https://" + bucketName + "." + endpoint.substring("https://".length()) + "/" + objectName;

            System.out.println(url);

            return url;

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }


    public String upload(MultipartFile file) throws Exception {
       return upload(file.getInputStream(), file.getOriginalFilename());
    }

    public String upload(File file, String fileName) throws Exception {
        return upload(new FileInputStream(file), fileName);
    }


}
