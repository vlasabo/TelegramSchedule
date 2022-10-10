package com.doctrine7.tgbot.service;

import com.doctrine7.tgbot.model.User;
import com.doctrine7.tgbot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SheduleUpdateMessageSender {
	private String lastEmployee;
	private String lastProcedure;
	private String lastPatient;
	private String lastTime;
	private String employee;
	private String procedure;
	private String patient;
	private String time;


	public SheduleUpdateMessageSender(String lastEmployee, String lastProcedure, String lastPatient, String lastTime,
									  String employee, String procedure, String patient, String time) {
		this.lastEmployee = lastEmployee;
		this.lastProcedure = lastProcedure;
		this.lastPatient = lastPatient;
		this.lastTime = lastTime;
		this.employee = employee;
		this.procedure = procedure;
		this.patient = patient;
		this.time = time;
	}

	public SheduleUpdateMessageSender() {

	}

	private Map<String, String> getMessagesWhatsHappening() {
		if (lastPatient.equals(patient) && lastEmployee.equals(employee) && lastProcedure.equals(procedure)
				&& lastTime.equals(time)) {
			return null; //изменили комментарий, подтверждение или что-то еще несущественное для отправки
		}

		HashMap<String, String> employeeAndMessage = new HashMap<>();
		StringBuilder sb = new StringBuilder();

		if (lastPatient.equals("") & lastProcedure.equals("")) {//Добавление пациента изменением пустой клетки
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

		String message =
				sb.append("Произошли изменения,\n    БЫЛО: ").append(lastProcedure).append(" у ").append(lastEmployee)
						.append(", процедура ").append(lastProcedure).append(" в ").append(lastTime)
						.append(" у пациента ").append(lastPatient).append(",\n   СТАЛО: ")
						.append(procedure).append(" у ").append(employee).append(", процедура ").append(procedure)
						.append(" в ").append(time).append(" у пациента ").append(patient).toString();
		employeeAndMessage.put(lastEmployee, message);
		employeeAndMessage.put(employee, message);
		return employeeAndMessage;
	}


	public void sendSheduleUpdate(TelegramBot telegramBot, UserRepository userRepository) throws TelegramApiException {
		var mapResultEmployeeMessage = getMessagesWhatsHappening();
		if (mapResultEmployeeMessage == null) {
			return;
		}
		var allUsersIterable = userRepository.findAll();

		for (Map.Entry<String, String> entry : mapResultEmployeeMessage.entrySet()) {
			for (User user : allUsersIterable) {
				if (user.getEmployees().contains(entry.getKey())) {
					telegramBot.sendMessageToId(user.getChatId(), entry.getValue());
					log.info("!sending a message about changes in the schedule to the employee {}, message = {}",
							entry.getKey(), entry.getValue());
				}
			}
		}
	}

}
