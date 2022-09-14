package com.doctrine7.TGbot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("application.properties")
public class ResponseToSqlConfig {

	@Value("${userSQL.name}")
	String botName;

	@Value("${userSQL.pwd}")
	String token;
}
