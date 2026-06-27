package com.campus.trading.service;

import com.campus.trading.dto.ImageToItemResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 图片识别服务 - 调用豆包视觉API,从图片生成商品标题、描述、预估价格
 */
@Slf4j
@Service
public class ImageToItemService {

    @Value("${llm.api-url:}")
    private String chatApiUrl;

    @Value("${llm.api-key:}")
    private String apiKey;

    @Value("${llm.model:doubao-seed-2-0-pro-260215}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImageToItemService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(60000);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 根据商品图片生成标题、描述、预估价格
     * @param base64Images base64编码的图片列表(只取第一张)
     */
    public ImageToItemResponse generateFromImage(List<String> base64Images) {
        if (chatApiUrl == null || chatApiUrl.isBlank()) {
            log.warn("LLM API未配置,无法识别图片");
            return null;
        }

        String responsesUrl = chatApiUrl.replace("/chat/completions", "/responses");

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);

            // 关闭思考模式
            Map<String, String> thinking = new HashMap<>();
            thinking.put("type", "disabled");
            requestBody.put("thinking", thinking);

            // 构建多模态内容: 图片 + 文字提示
            List<Map<String, Object>> contentParts = new ArrayList<>();

            // 图片(取第一张)
            if (!base64Images.isEmpty()) {
                Map<String, Object> imagePart = new HashMap<>();
                imagePart.put("type", "input_image");
                imagePart.put("image_url", base64Images.get(0));
                contentParts.add(imagePart);
            }

            // 文字提示
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("type", "input_text");
            textPart.put("text", buildPrompt());
            contentParts.add(textPart);

            // 用户消息(仅单条,无上下文)
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", contentParts);

            requestBody.put("input", Collections.singletonList(userMessage));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("调用豆包视觉API识别商品图片...");
            ResponseEntity<String> response = restTemplate.exchange(
                    responsesUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("视觉API响应: {}", response.getBody().substring(0, Math.min(300, response.getBody().length())));
                return parseResponse(response.getBody());
            } else {
                log.warn("视觉API返回异常状态: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("图片识别失败: {}", e.getMessage());
        }

        return null;
    }

    private String buildPrompt() {
        return "根据图片中的商品，生成以下信息：\n" +
               "1. 物品标题（简洁明了,不超过50字）\n" +
               "2. 物品描述（包含品牌型号、成色、使用情况等,不超过500字）\n" +
               "3. 预估二手价格（单位：元,纯数字）\n\n" +
               "请严格按以下JSON格式输出,不要任何其他内容：\n" +
               "{\"title\":\"标题\",\"description\":\"描述\",\"price\":数字}";
    }

    /**
     * 解析视觉API响应,提取商品信息
     */
    private ImageToItemResponse parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // 火山引擎responses API返回格式: output[0].content[0].text
            String text = null;
            JsonNode output = root.path("output");
            if (output.isArray() && output.size() > 0) {
                JsonNode content = output.get(0).path("content");
                if (content.isArray() && content.size() > 0) {
                    text = content.get(0).path("text").asText();
                }
            }

            if (text == null || text.isBlank()) {
                // 兼容chat/completions格式: choices[0].message.content
                text = root.path("choices").get(0)
                        .path("message").path("content").asText();
            }

            if (text != null && !text.isBlank()) {
                return extractFromText(text);
            }
        } catch (Exception e) {
            log.error("解析视觉API响应失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从模型返回的文本中提取标题、描述、价格
     * 优先解析JSON,失败则用正则兜底
     */
    private ImageToItemResponse extractFromText(String text) {
        // 尝试解析JSON
        try {
            // 提取可能的JSON块
            String json = text.trim();
            int start = json.indexOf('{');
            int end = json.lastIndexOf('}');
            if (start >= 0 && end > start) {
                json = json.substring(start, end + 1);
                JsonNode node = objectMapper.readTree(json);
                String title = node.path("title").asText(null);
                String description = node.path("description").asText(null);
                Double price = node.path("price").asDouble(0);

                if (title != null && !title.isBlank()) {
                    return ImageToItemResponse.of(title, description, price);
                }
            }
        } catch (Exception ignored) {
            // JSON解析失败,用正则兜底
        }

        // 正则兜底
        String title = extractField(text, "标题[：:]\\s*", 50);
        String description = extractField(text, "描述[：:]\\s*", 500);
        Double price = extractPrice(text);

        if (title != null && !title.isBlank()) {
            return ImageToItemResponse.of(title, description, price != null ? price : 0);
        }
        return null;
    }

    private String extractField(String text, String prefixRegex, int maxLen) {
        Pattern p = Pattern.compile(prefixRegex + "(.+?)(?:\\n|$)");
        Matcher m = p.matcher(text);
        if (m.find()) {
            String value = m.group(1).trim();
            return value.length() > maxLen ? value.substring(0, maxLen) : value;
        }
        return null;
    }

    private Double extractPrice(String text) {
        Pattern p = Pattern.compile("价格[：:]\\s*(\\d+(?:\\.\\d{1,2})?)");
        Matcher m = p.matcher(text);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        // 兜底: 匹配任意 "数字元" 格式
        p = Pattern.compile("(\\d+(?:\\.\\d{1,2})?)\\s*元");
        m = p.matcher(text);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return 0.0;
    }
}
