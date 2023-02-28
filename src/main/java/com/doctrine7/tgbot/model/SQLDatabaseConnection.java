package com.doctrine7.tgbot.model;


import com.doctrine7.tgbot.config.ResponseToSqlConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final String database;
    private final String connectionUrl;

    @Autowired
    public SQLDatabaseConnection(ResponseToSqlConfig config) {
        String userSQLname = config.getName();
        String userSQLpwd = config.getPassword();
        this.database = config.getDatabase();
        String address = config.getAddress();
        String port = config.getPort();
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
            //+2000 из-за особенностей хранения дат 1С в MSSQL
            String stringDate = date.plusYears(2000).format(formatterForRequest);
            String selectSql =
                    new StringBuilder()
                            .append("SELECT u._Description as procedure, o._Fld1043,")
                            .append(" s._Description as empl, p._Description as patient,")
                            .append(" x._Description as subEmpl ")
                            .append("FROM [").append(database).append("].[dbo].[_InfoRg970] AS o ")
                            .append("LEFT JOIN  [").append(database)
                            .append("].[dbo].[_Reference16] AS u ON u._IDRRef = o._Fld1041RRef ")
                            .append("LEFT JOIN [").append(database)
                            .append("].[dbo].[_Reference17] AS s ON s._IDRRef = o._Fld1040_RRRef ")
                            .append("LEFT JOIN [").append(database)
                            .append("].[dbo].[_Reference8] AS p ON p._IDRRef = o._Fld1042RRef ")
                            .append("LEFT JOIN [").append(database)
                            .append("].[dbo].[_Reference1710] AS x ON o._Fld1040_RRRef = x._IDRRef ")
                            .append("WHERE o._Period='").append(stringDate).append("' ")
                            .append("ORDER BY o._Fld1043").toString();

            ResultSet resultSet = statement.executeQuery(selectSql);
            List<Shedule> allShedule = new ArrayList<>();
            while (resultSet.next()) {
                String employee = resultSet.getString("empl");
                if (employee == null) {
                    employee = resultSet.getString("subEmpl");
                }
                Shedule shedule = new Shedule(LocalDateTime.parse(resultSet.getString("_Fld1043"), formatterForParsing)
                        .format(formatterForAnswer)
                        , employee
                        , resultSet.getString("patient")
                        , resultSet.getString("procedure"));
                allShedule.add(shedule);
            }
            return allShedule;
        } catch (SQLException e) {
            log.error("Error when requesting a schedule! \n" + e.getMessage());
        }
        return null;
    }

    public String sendRegistrationRequest(String name) { // Параметр "сотрудник из 1С". Полные тёзки не поддерживаются
        try {
            Connection connection = DriverManager.getConnection(connectionUrl);
            String selectSql = new StringBuilder().append("SELECT _Description ")
                    .append("  FROM [").append(database).append("].[dbo].[_Reference17] as empl ")
                    .append("  WHERE empl._Fld963!=0x01 /*уволен*/ AND empl._Marked=0x00 /*пометка удаления*/")
                    .append("  AND empl._Description=VALUES(?)").toString();
            PreparedStatement statement = connection.prepareStatement(selectSql);
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery(selectSql);

            if (resultSet.next()) {
                log.info("new SQL request for add employee {}", name);
                return resultSet.getString("_Description");
            }
        } catch (SQLException e) {
            log.error(String.format("Error when add employee %s \n", name) + e.getMessage());
        }
        return "";
    }

    public List<String> findRelatedEmployeesInDatabase(String name) {
        List<String> relatedEmployees = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection(connectionUrl);
            String selectSql =
                    new StringBuilder()
                            .append("SELECT subEmpl._Description as subEmplName, empl._Description as emplName ")
                            .append("FROM [").append(database).append("].[dbo].[_Reference1710] as subEmpl ")
                            .append("INNER JOIN [").append(database).append("].[dbo].[_Reference17] as empl ")
                            .append("ON subEmpl._Fld4750RRef = empl._IDRRef ")
                            .append("WHERE empl._Description=VALUES(?)").toString();
            PreparedStatement statement = connection.prepareStatement(selectSql);
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery(selectSql);

            while (resultSet.next()) {
                String subEmployee = resultSet.getString("subEmplName");
                log.info("add sub_employee {} for employee {} ", subEmployee, name);
                relatedEmployees.add(subEmployee);
            }
        } catch (SQLException e) {
            log.error(String.format("Error when add sub_employee for employee %s! \n", name) + e.getMessage());

        }
        return relatedEmployees;
    }
}