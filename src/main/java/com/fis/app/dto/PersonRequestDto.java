package com.fis.app.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersonRequestDto<T> {
	private T id;
}
