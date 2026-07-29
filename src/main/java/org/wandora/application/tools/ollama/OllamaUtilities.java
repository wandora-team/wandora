package org.wandora.application.tools.ollama;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.ollama4j.Ollama;
import java.util.Properties;

public class OllamaUtilities {
	
	private static String defaultModelName;
	
	
    public static Ollama setUp() throws Exception {
        Ollama api;
        int requestTimeoutSeconds = 60;
        int numberOfRetriesForModelPull = 5;

        try {
            // Try to get from env vars first
            String useExternalOllamaHostEnv = System.getenv("USE_EXTERNAL_OLLAMA_HOST");
            String ollamaHostEnv = System.getenv("OLLAMA_HOST");

            boolean useExternalOllamaHost;
            String ollamaHost;

            if (useExternalOllamaHostEnv == null && ollamaHostEnv == null) {
                // Fallback to test-config.properties from classpath
                Properties props = new Properties();
                try {
                    props.load(
                    		OllamaUtilities.class
                                    .getClassLoader()
                                    .getResourceAsStream("ollama/ollama-config.properties"));
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Could not load ollama/ollama-config.properties from classpath", e);
                }
                useExternalOllamaHost =
                        Boolean.parseBoolean(
                                props.getProperty("USE_EXTERNAL_OLLAMA_HOST", "false"));
                ollamaHost = props.getProperty("OLLAMA_HOST");
                requestTimeoutSeconds =
                        Integer.parseInt(props.getProperty("REQUEST_TIMEOUT_SECONDS"));
                numberOfRetriesForModelPull =
                        Integer.parseInt(props.getProperty("NUMBER_RETRIES_FOR_MODEL_PULL"));
                
                defaultModelName = props.getProperty("DEFAULT_MODEL");
            } else {
                useExternalOllamaHost = Boolean.parseBoolean(useExternalOllamaHostEnv);
                ollamaHost = ollamaHostEnv;
            }

            if (useExternalOllamaHost) {
                System.out.println("Using external Ollama host...");
                api = new Ollama(ollamaHost);
            } else {
                throw new RuntimeException(
                        "USE_EXTERNAL_OLLAMA_HOST is not set so, we will be using Testcontainers"
                            + " Ollama host for the tests now. If you would like to use an external"
                            + " host, please set the env var to USE_EXTERNAL_OLLAMA_HOST=true and"
                            + " set the env var OLLAMA_HOST=http://localhost:11435 or a different"
                            + " host/port.");
            }
            
        } catch (Exception e) {
            throw new Exception("Could not setup Ollama API: " + e.getMessage());
        }
        api.setRequestTimeoutSeconds(requestTimeoutSeconds);
        api.setNumberOfRetriesForModelPull(numberOfRetriesForModelPull);
        return api;
    }
    
    
    public static String getDefaultModelName() {
    	return defaultModelName;
    }
    

    public static String getFromEnvVar(String key) {
        String val = System.getenv(key);
        if (val == null) {
            System.out.println("Environment variable " + key + " not found!");
        }
        return val;
    }

    /*
     * Returns the path to the resources folder.
     */
    public static String getResourcesFolderPath() {
        return OllamaUtilities.class.getResource("/").getPath();
    }

    public static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}