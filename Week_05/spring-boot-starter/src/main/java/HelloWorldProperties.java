import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author mark
 * @date 2020/11/22
 */
@ConfigurationProperties(prefix = "helloworld")
public class HelloWorldProperties {
    public static final String DEFAULT_WORDS = "world";

    private String words = DEFAULT_WORDS;

    public static String getDefaultWords() {
        return DEFAULT_WORDS;
    }

    public String getWords() {
        return words;
    }
}
