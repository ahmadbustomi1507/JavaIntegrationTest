package com.fis.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fis.app.dto.PersonDto;
import com.fis.app.dto.PersonRequestDto;
import com.fis.app.service.PersonService;

@RestController
public class PersonController {

	@Autowired
	private  PersonService personService;
	
	@PostMapping("/api/get-data")
	@Transactional
	public PersonDto getPerson (@RequestBody PersonRequestDto p) throws Exception {
		return personService.getPerson(p);
	}
	
	@PostMapping("/api/get-data-from-third-party")
	@Transactional
	public PersonDto getPersonFromThirdParty (@RequestBody PersonRequestDto p) throws Exception {
		return personService.getPersonFromThirdParty(p);
	}
}
