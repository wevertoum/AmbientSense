package com.ambientsense.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AmbientsenseProperties.class)
public class AmbientsenseConfig {
}
