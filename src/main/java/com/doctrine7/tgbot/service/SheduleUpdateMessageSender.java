package com.doctrine7.tgbot.service;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class SheduleUpdateMessageSender {
    private String lastEmployee;
    private String lastProcedure;
    private String lastPatient;
    private String lastTime;
    private final String employee;
    private final String procedure;
    private final String patient;
    private final String time;
    private final boolean delete;
    private final EmployeeService employeeService;


    public SheduleUpdateMessageSender(String lastEmployee, String lastProcedure, String lastPatient, String lastTime,
                                      String employee, String procedure, String patient, String time, EmployeeService employeeService) {
        this.lastEmployee = lastEmployee;
        this.lastProcedure = lastProcedure;
        this.lastPatient = lastPatient;
        this.lastTime = lastTime;
        this.employee = employee;
        this.procedure = procedure;
        this.patient = patient;
        this.time = time;
        this.delete = false;
        this.employeeService = employeeService;
    }

    public SheduleUpdateMessageSender(String employee, String procedure, String patient, String time, EmployeeService employeeService) {
        this.employee = employee;
        this.procedure = procedure;
        this.patient = patient;
        this.time = time;
        this.delete = true;
        this.employeeService = employeeService;
    }

    private Map<String, String> getMessagesWhatsHappening() {
        HashMap<String, String> employeeAndMessage = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        if (delete) {
            sb.append("Удалена процедура ").append(procedure).append(" у ").append(patient).append(" в ").append(time);
            employeeAndMessage.put(employee, sb.toString());
            return employeeAndMessage;
        }

        if (lastPatient.equals(patient) && lastEmployee.equals(employee) && lastProcedure.equals(procedure)
                && lastTime.equals(time)) {
            return employeeAndMessage; //изменили комментарий, подтверждение или что-то еще несущественное для отправки
        }

        if (lastPatient.equals("") & lastProcedure.equals("")) { //Добавление пациента изменением пустой клетки
            // расписания или добавление новой процедуры
            sb.append("Пациент ").append(patient).append(" был добавлен на ").append(time).append(", процедура ")
                    .append(procedure);
            employeeAndMessage.put(employee, sb.toString());
            return employeeAndMessage;
        }

        if (!lastEmployee.equals(employee)) {//Изменение сотрудника. Сформируем два сообщения для каждого
            sb.append("Удалена процедура ").append(procedure).append(" у ").append(patient).append(" на ").append(lastTime);
            employeeAndMessage.put(lastEmployee, sb.toString());
            sb.setLength(0);
            sb.append("Пациент ").append(patient).append(" был добавлен на ").append(time).append(", процедура ")
                    .append(procedure);
            employeeAndMessage.put(employee, sb.toString());
            return employeeAndMessage;
        }

        if (lastProcedure.equals(procedure) & lastPatient.equals(patient)) {
            sb.append("Пациент ").append(patient).append(" был перенесён с ").append(lastTime).append(" на ")
                    .append(time).append(", процедура ").append(procedure);
            employeeAndMessage.put(employee, sb.toString());
            return employeeAndMessage;
        }

        if (lastPatient.equals(patient) & !lastProcedure.equals(procedure) & lastTime.equals(time)) {
            sb.append("У пациента ").append(patient).append(" в ").append(time).append(" изменилась процедура с ")
                    .append(lastProcedure).append(" на ").append(procedure);
            employeeAndMessage.put(employee, sb.toString());
            return employeeAndMessage;
        }

        String message =
                sb.append("Произошли изменения,\n    БЫЛО: процедура ").append(lastProcedure)
                        .append(" у ").append(lastEmployee).append(" в ").append(lastTime)
                        .append(" у пациента ").append(lastPatient).append(",\n   СТАЛО: процедура ").append(procedure)
                        .append(" у ").append(employee).append(" в ").append(time)
                        .append(" у пациента ").append(patient).toString();
        employeeAndMessage.put(lastEmployee, message);
        employeeAndMessage.put(employee, message);
        return employeeAndMessage;
    }


    public void sendSheduleUpdate(TelegramBot telegramBot)
            throws TelegramApiException {
        Map<String, String> mapResultEmployeeMessage = getMessagesWhatsHappening(); //<employee, message>
        if (mapResultEmployeeMessage.size() == 0) {
            return;
        }

        List<String> allEmployeesWhoNeedMessage = mapResultEmployeeMessage.keySet().stream()
                .distinct()
                .collect(Collectors.toList());
        List<Long> allUsersIdsWhoNeedMessage = employeeService.findAllByEmployeeIn(allEmployeesWhoNeedMessage);
        Map<Long, Set<String>> userIdsAndEmployees =
                employeeService.getEmployeesNamesForListUsers(allUsersIdsWhoNeedMessage); //<userId, employeesNames>

        for (Map.Entry<String, String> employeeAndMessage : mapResultEmployeeMessage.entrySet()) {
            for (Map.Entry<Long, Set<String>> idAndSetEmployees : userIdsAndEmployees.entrySet()) {
                Set<String> userEmployees = idAndSetEmployees.getValue();
                if (userEmployees.contains(employeeAndMessage.getKey())) {
                    telegramBot.sendMessageToId(idAndSetEmployees.getKey(), employeeAndMessage.getValue());
                    log.info("!sending a message about changes in the schedule to the employee {}, message = {}",
                            employeeAndMessage.getKey(), employeeAndMessage.getValue());
                }
            }
        }
    }

}
