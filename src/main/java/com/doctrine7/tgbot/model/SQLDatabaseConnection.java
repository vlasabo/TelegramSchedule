package com.doctrine7.tgbot.model;


import com.doctrine7.tgbot.config.ResponseToSqlConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SQLDatabaseConnection {

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


	public List<Shedule> sendScheduleRequest(LocalDate date) {
		try {
			Connection connection = DriverManager.getConnection(connectionUrl);
			Statement statement = connection.createStatement();
			DateTimeFormatter formatterForRequest = DateTimeFormatter.ofPattern("yyyyMMdd");
			DateTimeFormatter formatterForParsing = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			DateTimeFormatter formatterForAnswer = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
			String stringDate = date.plusYears(2000).format(formatterForRequest); //+2000 cause 1C+mssql work strange
			String selectSql =
					"SELECT u._Description as procedura, o._Fld1043, s._Description as sotr, p._Description as " +
							"pacient, x._Description as podsotr\n" +
							"FROM [" + database + "].[dbo].[_InfoRg970] AS o\n" +
							"LEFT JOIN  [" + database + "].[dbo].[_Reference16] AS u ON u._IDRRef = o._Fld1041RRef\n" +
							"LEFT JOIN [" + database + "].[dbo].[_Reference17] AS s ON s._IDRRef = o._Fld1040_RRRef\n" +
							"LEFT JOIN [" + database + "].[dbo].[_Reference8] AS p ON p._IDRRef = o._Fld1042RRef\n" +
							"LEFT JOIN [" + database + "].[dbo].[_Reference1710] AS x ON o._Fld1040_RRRef = x._IDRRef\n" +
							"WHERE o._Period='" + stringDate + "'\n" + "ORDER BY o._Fld1043";

			ResultSet resultSet = statement.executeQuery(selectSql);
			List<Shedule> allShedule = new ArrayList<>();
			while (resultSet.next()) {
				String sotr = resultSet.getString("sotr");
				if (sotr == null) {
					sotr = resultSet.getString("podsotr");
				}
				Shedule shedule = new Shedule(LocalDateTime.parse(resultSet.getString("_Fld1043"), formatterForParsing)
						.format(formatterForAnswer)
						, sotr
						, resultSet.getString("pacient")
						, resultSet.getString("procedura"));
				allShedule.add(shedule);
			}
			return allShedule;
		} catch (SQLException e) {
			log.error("Error when requesting a schedule! \n" + e.getMessage());
		}
		return null;
	}

	public String sendRegistrationRequest(String name) { // ???????????????? "?????????????????? ???? 1??". ???????????? ?????????? ???? ????????????????????????????
		//TODO: ?????????????????????? ?? ??????????????????????!
		if ((name.toLowerCase().contains("select")) || name.toLowerCase().contains("drop")
				|| name.toLowerCase().contains("insert") || name.toLowerCase().contains("update")
				|| name.toLowerCase().contains("delete") || name.toLowerCase().contains("create")
				|| name.toLowerCase().contains("--") || name.toLowerCase().contains("/*")) {
			log.error("?????????????????????? ?????????? ?????? ?????????????? ?? ?????????? ?????? ??????????????????????!");
			return "";
		}
		try {
			Connection connection = DriverManager.getConnection(connectionUrl);
			Statement statement = connection.createStatement();
			String selectSql =
					String.format("SELECT _Description\n" +
							"  FROM [" + database + "].[dbo].[_Reference17] as sotr\n" +
							"  WHERE sotr._Fld963!=0x01 /*????????????*/ AND sotr._Marked=0x00 /*?????????????? ????????????????*/" +
							"  AND sotr._Description='%s'", name);


			//SqlRowSet likesRows = jdbcTemplate.queryForRowSet("SELECT _Description\n" +
			//				"  FROM [dorabotka].[dbo].[_Reference17] as sotr\n" +
			//				"  WHERE sotr._Fld963!=0x01 /*????????????*/ AND sotr._Marked=0x00 /*?????????????? ????????????????*/" +
			//"  AND sotr._Description=?", name);
			//if (likesRows.next()) {
			// Optional<String> sotrOptional = likesRows.getString("_Description");
			//if (sotrOptional.isPresent()) {
			//   return sotrOptional.get()
			//}
			ResultSet resultSet = statement.executeQuery(selectSql);


			if (resultSet.next()) {
				log.info("new SQL request for add employee {}", name);
				return resultSet.getString("_Description");
			}
		} catch (SQLException e) {
			log.error("Error when add employee! \n" + e.getMessage());
		}
		return "";
	}

	public List<String> checkRelatedEmployees(String name) {
		List<String> relatedEmployees = new ArrayList<>();
		try {
			Connection connection = DriverManager.getConnection(connectionUrl);
			Statement statement = connection.createStatement();
			String selectSql =
					String.format("SELECT podsotr._Description as podsotrName,sotr._Description as sotrName\n" +
							"FROM [" + database + "].[dbo].[_Reference1710] as podsotr\n" +
							"INNER JOIN [" + database + "].[dbo].[_Reference17] as sotr ON podsotr._Fld4750RRef = sotr._IDRRef\n" +
							"WHERE sotr._Description='%s'", name);

			ResultSet resultSet = statement.executeQuery(selectSql);


			while (resultSet.next()) {
				String emp = resultSet.getString("podsotrName");
				log.info("add sub_employee for employee {} ", emp);
				relatedEmployees.add(emp);
			}
		} catch (SQLException e) {
			log.error("Error when add sub_employee for employee {}!", name + "\n" + e.getMessage());
		}
		return relatedEmployees;
	}
}