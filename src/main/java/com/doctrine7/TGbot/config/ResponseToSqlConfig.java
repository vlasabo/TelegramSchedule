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
	private String name;

	@Value("${userSQL.pwd}")
	private String password;

	@Value("${database.name}")
	private String database;

	@Value("${database.address}")
	private String address;

	@Value("${database.port}")
	private String port;
}
