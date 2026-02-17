package ru.papkov.repairlog.application.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Стандартный формат ответа с ошибкой.
 *
 * @author aim-41tt
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

	private int status;
	private String error;
	private String message;
	private String path;
	private LocalDateTime timestamp;
	private List<FieldError> fieldErrors;

	public ApiError() {
		this.timestamp = LocalDateTime.now();
	}

	public ApiError(int status, String error, String message, String path) {
		this();
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public List<FieldError> getFieldErrors() {
		return fieldErrors;
	}

	public void setFieldErrors(List<FieldError> fieldErrors) {
		this.fieldErrors = fieldErrors;
	}

	/**
	 * Описание ошибки валидации конкретного поля.
	 */
	public static class FieldError {
		private String field;
		private String message;

		public FieldError() {
		}

		public FieldError(String field, String message) {
			this.field = field;
			this.message = message;
		}

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
}
