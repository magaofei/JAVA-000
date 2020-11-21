package com.magaofei;

import org.springframework.context.annotation.Bean;

/**
 * @author mark
 * @date 2020/11/21
 */
public class StudentBean {

    @Bean
    public Student student() {
        return new Student();
    }

}
