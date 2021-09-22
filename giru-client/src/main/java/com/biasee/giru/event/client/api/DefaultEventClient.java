package com.biasee.giru.event.client.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


@Component
public class DefaultEventClient implements EventClient {


    private String eventURL = "";

    @Autowired
    public DefaultEventClient(@Value("${giru.event.url}") String eventURL) {
        this.eventURL = eventURL;
    }

    @Override
    public void report(String type, String event) {
        post(type, event);
    }

    private void post(String type, String json) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(eventURL + "/event/report/" + type);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            // 超时时间
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            // 设置是否输出
            connection.setDoOutput(true);
            // 设置是否读入
            connection.setDoInput(true);
            // 设置是否使用缓存
            connection.setUseCaches(false);
            // 设置此 HttpURLConnection 实例是否应该自动执行 HTTP 重定向
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            // 连接
            connection.connect();

            // 写入参数到请求中
            OutputStream out = connection.getOutputStream();
            out.write(json.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
            int responseCode = connection.getResponseCode();
            connection.getInputStream().close();
            if (responseCode != 200) {
                throw new Exception("Post failed, code:" + responseCode);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
