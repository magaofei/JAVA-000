package com.magaofei;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author magaofei
 * @date 2020/11/22
 */
@EnableAutoConfiguration
@Configuration
public class StudentAutoConfiguration {

    @Bean
    public Student newStudent() {
        return new Student();
    }
}
