package com.fis.app.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ResponseThirdPartyDto {

	private LocalDateTime createdAt;
	
	private String name;
	
	private String email;
	
	private Boolean status;
	
}
