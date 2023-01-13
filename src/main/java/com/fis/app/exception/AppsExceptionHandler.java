package com.fis.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AppsExceptionHandler {

	@ExceptionHandler(value = PersonException.class)
	public ResponseEntity<Object> PersonException(PersonException exception) {
		return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<Object> exception(Exception exception) {
		return new ResponseEntity<>("Opps something wents wrong", HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
