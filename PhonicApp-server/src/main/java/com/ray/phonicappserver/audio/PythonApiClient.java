package com.ray.phonicappserver.audio;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class PythonApiClient {

    private static final String PYTHON_API_URL = "http://localhost:9000/similarity";

    public String sendToPython(File file1, File file2) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(PYTHON_API_URL);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file1", file1, ContentType.DEFAULT_BINARY, file1.getName());
            builder.addBinaryBody("file2", file2, ContentType.DEFAULT_BINARY, file2.getName());

            HttpEntity entity = builder.build();
            request.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(request)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }
}
