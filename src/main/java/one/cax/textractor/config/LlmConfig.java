package one.cax.textractor.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAI LLM service.
 */
@Configuration
public class LlmConfig {

    @Value("${openai.model:gpt-4}")
    private String model;

    @Value("${openai.temperature:0.7}")
    private double temperature;

    @Value("${openai.max-tokens:2000}")
    private int maxTokens;

    @Value("${openai.api-key:}")
    private String apiKey;


    @Bean
    public OpenAiChatModel openaiChatModel() {
        // Configure and return an instance of OpenAiChatModel
        OpenAiApi openAiApi = OpenAiApi.builder()
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .build();
    }

    /**
     * Get the OpenAI model to use
     * 
     * @return OpenAI model name
     */
    public String getModel() {
        return model;
    }

    /**
     * Get the temperature setting for OpenAI
     * 
     * @return Temperature value
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Get the maximum tokens for OpenAI responses
     * 
     * @return Maximum tokens
     */
    public int getMaxTokens() {
        return maxTokens;
    }

    /**
     * Get the OpenAI API key
     * 
     * @return API key
     */
    public String getApiKey() {
        return apiKey;
    }
}
