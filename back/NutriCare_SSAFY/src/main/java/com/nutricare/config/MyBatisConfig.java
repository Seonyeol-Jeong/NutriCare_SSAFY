package com.nutricare.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.nutricare.model.dao")
public class MyBatisConfig {

}
