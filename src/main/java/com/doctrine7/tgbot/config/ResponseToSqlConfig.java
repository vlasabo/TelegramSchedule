package com.doctrine7.tgbot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
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
