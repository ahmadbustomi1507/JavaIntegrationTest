package com.fis.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fis.app.dto.PersonDto;
import com.fis.app.dto.PersonRequestDto;
import com.fis.app.dto.ResponseThirdPartyDto;
import com.fis.app.entity.Person;
import com.fis.app.exception.PersonException;
import com.fis.app.repository.PersonRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class PersonService {

	@Value("${demo.service.test.api-third-party}")
	private String apiThirdParty;
	@Autowired
	private RedisTemplate<String, String> redistemplate;
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private PersonRepository personRepository;
	
	public PersonDto getPerson(PersonRequestDto dto) {
		
		log.info("Incoming request getPerson {}", dto.getId());
		
		PersonDto d = new PersonDto();
		
		Person p = personRepository.findById((Integer) dto.getId())
				.orElseThrow(()->new PersonException("Id Not Found"));

		d.setId(p.getId());
		d.setName(p.getName());
		d.setEmail(p.getEmail());
		
		return d;
	}
	
	public PersonDto getPersonFromThirdParty(PersonRequestDto p) {
		
		String queryParam = apiThirdParty+p.getId();

		log.info("Incoming request getPersonFromThirdParty {}", p.getId());
		log.info("Hit api \n" + queryParam);

		ResponseEntity<ResponseThirdPartyDto> res =
				restTemplate.getForEntity(queryParam, ResponseThirdPartyDto.class);

		log.info("Response from mock API : \n" + res.getBody().toString() + "status code " + Integer.toString(res.getStatusCode().value()));
		if(!res.getStatusCode().is2xxSuccessful()) {
			throw new PersonException("Error access to third party API");
		}
		
		PersonDto d = new PersonDto();

		d.setEmail(res.getBody().getEmail());
		d.setId((Integer) p.getId());
		d.setName(res.getBody().getName());
		
		return d;
	}

	public void save(PersonDto p) {

		log.info("Incoming request savePerson {} from kafka", p.getId());

		Person person = new Person();

		person.setId(p.getId());
		person.setName(p.getName());
		person.setEmail(p.getEmail());

		personRepository.save(person);
	}

	public Person getpersonRedis(PersonRequestDto p) throws JsonMappingException, JsonProcessingException {

		String ps= null;
		try {
			ps = redistemplate.opsForValue().get(p.getId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(ps ==null) {
			throw new PersonException("Id Not Found");
		}

		Person person = new ObjectMapper().readValue(ps, Person.class);

		return person;
	}
}
