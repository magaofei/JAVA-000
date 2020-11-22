import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author magaofei
 * @date 2020/11/22
 */
@Configuration
@ConditionalOnClass({HelloWorldService.class})
@EnableConfigurationProperties(HelloWorldProperties.class)
public class HelloWorldAutoConfiguration {

    @Autowired
    private HelloWorldProperties helloWorldProperties;

    // 当容器没有这个 Bean 的时候才创建这个 Bean
    @Bean
    @ConditionalOnMissingBean(HelloWorldService.class)
    public HelloWorldService helloWorldService() {
        HelloWorldService helloWorldService = new HelloWorldService();
        helloWorldService.setWords(helloWorldProperties.getWords());
        return helloWorldService;
    }

}
