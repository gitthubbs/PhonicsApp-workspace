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

    public String sendToPython(File file1, String targetText) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(PYTHON_API_URL);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file1", file1, ContentType.DEFAULT_BINARY, file1.getName());

            // 添加目标文本参数
            if (targetText != null && !targetText.isEmpty()) {
                builder.addTextBody("target_text", targetText, ContentType.TEXT_PLAIN);
            }

            HttpEntity entity = builder.build();
            request.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode != 200) {
                    throw new RuntimeException("Python API returned " + statusCode + ": " + responseBody);
                }
                return responseBody;
            }
        }
    }
}
