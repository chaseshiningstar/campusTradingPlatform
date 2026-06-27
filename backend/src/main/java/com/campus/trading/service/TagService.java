package com.campus.trading.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 标签生成服务 - 调用大模型API生成商品标签
 */
@Slf4j
@Service
public class TagService {

    @Value("${llm.api-url:}")
    private String apiUrl;

    @Value("${llm.api-key:}")
    private String apiKey;

    @Value("${llm.model:gpt-3.5-turbo}")
    private String model;

    private final RestTemplate restTemplate;

    public TagService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);  // 连接超时10秒
        factory.setReadTimeout(50000);      // 读取超时50秒
        this.restTemplate = new RestTemplate(factory);
    }
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据商品标题和描述生成6个搜索标签
     */
    public List<String> generateTags(String title, String description) {
        // 前置校验: 描述质量
        validateDescription(title, description);

        if (apiUrl == null || apiUrl.isBlank()) {
            log.warn("LLM API未配置,使用默认标签提取");
            return extractTagsLocally(title, description);
        }

        try {
            String prompt = buildPrompt(title, description);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 100);

            // 关闭思考模式,减少延迟和token消耗
            Map<String, String> thinking = new HashMap<>();
            thinking.put("type", "disabled");
            requestBody.put("thinking", thinking);

            // 仅单条user消息,无上下文
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);
            requestBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String content = root.path("choices").get(0)
                        .path("message").path("content").asText();

                // 检测AI返回的错误标记
                if (content != null && content.contains("[ERROR_DESC]")) {
                    throw new RuntimeException("物品描述过于简单或内容混乱，请提供更详细的描述信息（如品牌型号、使用情况、成色等）。推荐使用AI辅助描述");
                }

                List<String> tags = parseTags(content.trim());

                // 后置校验: 生成标签数不足,视为描述质量差
                if (tags.size() < 3) {
                    throw new RuntimeException("物品描述信息不足，仅生成了" + tags.size() + "个标签。请补充详细的描述信息（品牌型号、使用情况、成色等）");
                }

                return tags;
            }

        } catch (RuntimeException e) {
            // 业务异常(如描述校验失败)直接向上抛出,不降级
            throw e;
        } catch (Exception e) {
            log.error("调用大模型API失败,降级为本地提取: {}", e.getMessage());
        }

        // 大模型不可用时,降级为本地自动提取
        return extractTagsLocally(title, description);
    }

    /**
     * 构建 Prompt
     */
    private String buildPrompt(String title, String description) {
        return String.format(
                "根据以下商品信息生成6个精准的搜索标签。\n" +
                "要求：\n" +
                "1. 标签用中文,每个标签2-6个字。不允许自己发挥，只能根据物品描述来。如果有准确的商品款式，可以联网搜索\n" +
                "2. 涵盖品类、品牌、成色、核心卖点\n" +
                "3. 6个标签用逗号分隔,不要编号\n" +
                "4. 只输出标签,不要任何解释\n" +
                "5. 如果商品描述过于简单(只有几个字)或内容混乱、无法判断商品属性,请直接输出 [ERROR_DESC]\n\n" +
                "商品标题：%s\n" +
                "商品描述：%s\n\n" +
                "标签：", title, description);
    }

    /**
     * 解析标签字符串
     */
    private List<String> parseTags(String content) {
        // 清理和分割
        String cleaned = content.replaceAll("[\\[\\]\"\'\\n\\r]", "")
                .replaceAll("\\d+[、.．)]", "")
                .replaceAll("标签[：:]", "")
                .trim();

        List<String> tags = new ArrayList<>();
        for (String part : cleaned.split("[,，;；\\s]+")) {
            String tag = part.trim();
            if (!tag.isEmpty() && tag.length() >= 2 && tag.length() <= 8) {
                tags.add(tag);
            }
        }

        // 去重,最多6个
        Set<String> seen = new LinkedHashSet<>();
        for (String t : tags) {
            if (seen.size() >= 6) break;
            seen.add(t);
        }

        return new ArrayList<>(seen);
    }

    /**
     * 前置校验描述质量: 拒绝过于简单、混乱或无效的描述
     */
    private void validateDescription(String title, String description) {
        String desc = description != null ? description.trim() : "";
        String tit = title != null ? title.trim() : "";

        // 1. 描述为空或过短
        if (desc.length() < 6) {
            throw new RuntimeException("物品描述过于简单（少于6个字），请提供更详细的描述信息（如品牌型号、使用情况、成色等）");
        }

        // 2. 描述中有效中文字符太少
        long chineseCount = desc.codePoints().filter(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN).count();
        if (chineseCount < 4) {
            throw new RuntimeException("物品描述中有效信息不足，请用中文详细描述商品（品牌型号、使用情况、成色等）");
        }

        // 3. 标题+描述合并后信息量不足
        if (desc.length() < 15 && tit.length() < 5) {
            throw new RuntimeException("物品标题和描述信息量不足，请补充详细的商品信息");
        }

        // 4. 描述全是重复字符/无意义内容
        Set<Integer> uniqueChars = new HashSet<>();
        desc.codePoints().forEach(uniqueChars::add);
        if (uniqueChars.size() < 4) {
            throw new RuntimeException("物品描述内容混乱，请提供有意义的商品描述信息");
        }
    }

    /**
     * 本地降级方案: 从标题按空格/标点拆分提取标签
     */
    private List<String> extractTagsLocally(String title, String description) {
        String text = (title + " " + (description != null ? description : ""));
        // 按空格、逗号、中文标点拆分
        String[] words = text.split("[\\s,，。！？、；：\"'（）\\[\\]【】]+");

        // 过滤: 至少2个字,过滤无意义词
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "一个", "这个", "那个", "使用", "可以", "没有", "已经",
                "因为", "所以", "但是", "而且", "或者", "如果", "虽然",
                "什么", "怎么", "哪里", "非常", "比较", "特别", "一些",
                "一下", "等等", "其他", "所有", "各种", "一般", "真的"
        ));

        Set<String> seen = new LinkedHashSet<>();
        for (String w : words) {
            String word = w.trim();
            if (word.length() >= 2 && word.length() <= 8
                    && !stopWords.contains(word)
                    && !word.matches("\\d+")) {
                if (seen.size() >= 6) break;
                seen.add(word);
            }
        }

        return new ArrayList<>(seen);
    }
}
