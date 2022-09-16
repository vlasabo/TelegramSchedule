package com.doctrine7.TGbot.model;


import com.doctrine7.TGbot.config.ResponseToSqlConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Slf4j
@Component
public class SQLDatabaseConnection {

	private String response = ""; //not null cause we have .length() in TelegramBot

	private final ResponseToSqlConfig config;
	private final String userSQLname;
	private final String userSQLpwd;
	private final String database;
	private final String connectionUrl;
	private final String address;
	private final String port;


	public SQLDatabaseConnection(ResponseToSqlConfig config) {
		this.config = config;
		this.userSQLname = config.getName();
		this.userSQLpwd = config.getPassword();
		this.database = config.getDatabase();
		this.address = config.getAddress();
		this.port = config.getPort();
		this.connectionUrl = "jdbc:sqlserver://" + address + ":" + port + ";"
				+ "encrypt=false;"
				+ "database=" + database + ";"
				+ "user=" + userSQLname + ";"
				+ "password=" + userSQLpwd + ";"
				+ "trustServerCertificate=false;"
				+ "loginTimeout=30;";
	}

	public String getResponse() {
		return response;
	}

	public void sendRequest(LocalDate date) { //TODO:после добавления авторизации добавить в запрос параметр "сотрудник"
		try {
			Connection connection = DriverManager.getConnection(connectionUrl);
			Statement statement = connection.createStatement();
			DateTimeFormatter formatterForRequest = DateTimeFormatter.ofPattern("yyyyMMdd");
			DateTimeFormatter formatterForParsing = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			DateTimeFormatter formatterForAnswer = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
			String stringDate = date.plusYears(2000).format(formatterForRequest); //+2000 cause 1C+mssql work strange
			String selectSql =
					"SELECT u._Description as procedura, o._Fld1043, s._Description as sotr, p._Description as pacient\n" +
							"FROM [" + database + "].[dbo].[_InfoRg970] AS o\n" +
							"LEFT JOIN  [" + database + "].[dbo].[_Reference16] AS u ON u._IDRRef = o._Fld1041RRef\n" +
							"LEFT JOIN [" + database + "].[dbo].[_Reference17] AS s ON s._IDRRef = o._Fld1040_RRRef\n" +
							"LEFT JOIN [" + database + "].[dbo].[_Reference8] AS p ON p._IDRRef = o._Fld1042RRef\n" +
							"WHERE o._Period='" + stringDate + "'\n" + "ORDER BY o._Fld1043";

			ResultSet resultSet = statement.executeQuery(selectSql);
			StringBuilder sb = new StringBuilder();

			while (resultSet.next()) {
				sb.append(LocalDateTime.parse(resultSet.getString("_Fld1043"), formatterForParsing)
								.format(formatterForAnswer)).append(" - ")
						.append(resultSet.getString("procedura"))
						.append(", ")
						.append(resultSet.getString("pacient"))
						.append("\n");
			}

			response = sb.toString();
			log.info("requested schedule for the date " + date);
		} catch (SQLException e) {
			log.error("Error when requesting a schedule! \n" + e.getMessage());
		}
	}
}