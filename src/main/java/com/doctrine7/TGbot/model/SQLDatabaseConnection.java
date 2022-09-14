package com.doctrine7.TGbot.model;


import com.doctrine7.TGbot.config.ResponseToSqlConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;


public class SQLDatabaseConnection {

	ResultSet resultSet = null;
	String response = null;


	final ResponseToSqlConfig config;
	final String userSQLname;
	final String userSQLpwd;
	final String connectionUrl;


	public SQLDatabaseConnection(ResponseToSqlConfig config) {
		this.config = config;
		this.userSQLname = config.getBotName();
		this.userSQLpwd = config.getToken();
		this.connectionUrl = "jdbc:sqlserver://192.168.1.211:1433;"
				+ "encrypt=false;"
				+ "database=dorabotka;"
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
			DateTimeFormatter formatterForPasring = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			DateTimeFormatter formatterForAnswer = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
			String stringDate = date.plusYears(2000).format(formatterForRequest); //+2000 cause 1C+mssql work strange
			String selectSql =
					"SELECT u._Description as procedura, o._Fld1043, s._Description as sotr, p._Description as pacient\n" +
							"FROM [dorabotka].[dbo].[_InfoRg970] AS o\n" +
							"LEFT JOIN  [dorabotka].[dbo].[_Reference16] AS u ON u._IDRRef = o._Fld1041RRef\n" +
							"LEFT JOIN [dorabotka].[dbo].[_Reference17] AS s ON s._IDRRef = o._Fld1040_RRRef\n" +
							"LEFT JOIN [dorabotka].[dbo].[_Reference8] AS p ON p._IDRRef = o._Fld1042RRef\n" +
							"WHERE o._Period='" + stringDate + "'\n" + "ORDER BY o._Fld1043";

			resultSet = statement.executeQuery(selectSql);
			StringBuilder sb = new StringBuilder();

			while (resultSet.next()) {
				sb.append(LocalDateTime.parse(resultSet.getString("_Fld1043"), formatterForPasring)
								.format(formatterForAnswer)).append(" - ")
						.append(resultSet.getString("procedura"))
						.append(", ")
						.append(resultSet.getString("pacient"))
						.append("\n");
			}

			response = sb.toString();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}