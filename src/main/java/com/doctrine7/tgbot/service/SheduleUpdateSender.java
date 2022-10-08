package com.doctrine7.tgbot.service;

import java.util.HashMap;
import java.util.Map;

public class SheduleUpdateSender {
	private final String lastEmployee;
	private final String lastProcedure;
	private final String lastPatient;
	private final String lastTime;
	private final String employee;
	private final String procedure;
	private final String patient;
	private final String time;

	public SheduleUpdateSender(String lastEmployee, String lastProcedure, String lastPatient, String lastTime,
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

	public Map<String, String> getMessagesWhatsHappening() {
		HashMap<String, String> employeeAndMessage = new HashMap<>();
		StringBuilder sb = new StringBuilder();

		if (lastEmployee.equals("") & lastProcedure.equals("")) {//Добавление пациента изменением пустой клетки
			// расписания
			sb.append("Пациент ").append(patient).append(" был добавлен на ").append(time).append(", процедура ")
					.append(procedure);
			employeeAndMessage.put(employee, sb.toString());
			sb.setLength(0);
		}

		if (!lastEmployee.equals(employee)) {//Изменение сотрудника. Сформируем два сообщения для каждого
			sb.append("Удалена процедура ").append(procedure).append(" у ").append(patient).append(" на ").append(lastTime);
			employeeAndMessage.put(lastEmployee, sb.toString());
			sb.setLength(0);
			sb.append("Пациент ").append(patient).append(" был добавлен на ").append(time).append(", процедура ")
					.append(procedure);
			employeeAndMessage.put(employee, sb.toString());
			sb.setLength(0);
		}

		if (lastEmployee.equals(employee) & lastProcedure.equals(procedure) & lastPatient.equals(patient)) {
			//Изменение времени расписания
			//     ОповещениеВК.отправитьСообщениеСотруднику(СотрОпов1,"Пациент "+ объект.Пациент.Наименование +
			//     " был перенесён с " + Лев(Формат(объект.ВремяНачала1,"ДЛФ=В"),5)+ " на " + Лев(Формат(объект.ВремяНачала,"ДЛФ=В"),5) + ", процедура " + объект.Процедура);
			sb.append("Пациент ").append(patient).append(" был перенесён с ").append(lastTime).append(" на ")
					.append(time).append(", процедура ").append(procedure);
			employeeAndMessage.put(employee, sb.toString());
		}
		return employeeAndMessage;
	}
}
