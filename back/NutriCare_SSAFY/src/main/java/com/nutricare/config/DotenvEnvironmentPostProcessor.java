package com.nutricare.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * 간단한 .env 로더.
 * <p>
 * 동작 원리:
 * - 우선 현재 작업 디렉터리(user.dir)의 `.env` 파일을 찾습니다.
 * - 없으면 클래스패스 루트의 `.env` 리소스를 확인합니다.
 * - 찾은 경우 `MapPropertySource`로 만들어 환경에 추가합니다.
 * - 추가할 때 우선순위는 가장 낮게 (`addLast`) 하여 시스템 환경변수나 커맨드라인 인수가 우선하도록 합니다.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "dotenvProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> map = new HashMap<>();

        // 1) 프로젝트 작업 디렉터리의 .env (user.dir)
        String userDir = System.getProperty("user.dir");
        Path envPath = Paths.get(userDir, ".env");
        if (Files.exists(envPath) && Files.isRegularFile(envPath)) {
            try (BufferedReader r = Files.newBufferedReader(envPath, StandardCharsets.UTF_8)) {
                readLinesToMap(r, map);
            } catch (Exception e) {
                // 읽기 실패해도 무시
            }
        } else {
            // 2) 클래스패스 루트의 .env
            try {
                Resource res = new ClassPathResource(".env");
                if (res.exists()) {
                    try (InputStream in = res.getInputStream();
                         BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                        readLinesToMap(r, map);
                    }
                }
            } catch (Exception e) {
                // 무시
            }
        }

        if (!map.isEmpty()) {
            MutablePropertySources sources = environment.getPropertySources();
            MapPropertySource p = new MapPropertySource(PROPERTY_SOURCE_NAME, map);
            // 가장 낮은 우선순위로 추가하여 시스템 환경변수/프로퍼티가 우선하도록 함
            sources.addLast(p);
        }
    }

    private void readLinesToMap(BufferedReader reader, Map<String, Object> map) throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            int idx = line.indexOf('=');
            if (idx <= 0) continue;
            String key = line.substring(0, idx).trim();
            String val = line.substring(idx + 1).trim();
            // 따옴표로 감싸여 있으면 제거
            if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                val = val.substring(1, val.length() - 1);
            }
            map.put(key, val);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
