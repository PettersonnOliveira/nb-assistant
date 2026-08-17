package br.com.petterson.nbassistant.storage;

import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.*;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "oci")
public class OciObjectStorageService implements StorageService {

    private final ObjectStorageClient client;
    private final String namespace;
    private final String bucketName;

    public OciObjectStorageService(
            @Value("${oci.config.file}") String configFile,
            @Value("${oci.config.profile}") String configProfile,
            @Value("${oci.objectstorage.namespace}") String namespace,
            @Value("${oci.objectstorage.bucket-name}") String bucketName) throws Exception {

        var authProvider = new ConfigFileAuthenticationDetailsProvider(configFile, configProfile);
        this.client = ObjectStorageClient.builder().build(authProvider);
        this.namespace = namespace;
        this.bucketName = bucketName;
    }

    @Override
    @SneakyThrows
    public String upload(MultipartFile file, String destinationPath) {
        PutObjectRequest request = PutObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(destinationPath)
                .putObjectBody(file.getInputStream())
                .contentLength(file.getSize())
                .build();

        client.putObject(request);
        return destinationPath;
    }

    @Override
    @SneakyThrows
    public byte[] download(String storagePath) {
        GetObjectRequest request = GetObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(storagePath)
                .build();

        GetObjectResponse response = client.getObject(request);

        try (InputStream in = response.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            in.transferTo(out);
            return out.toByteArray();
        }
    }

    @Override
    public void delete(String storagePath) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(storagePath)
                .build();

        client.deleteObject(request);
    }
}