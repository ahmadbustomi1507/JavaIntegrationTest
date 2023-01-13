package com.fis.app.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.fis.app.entity.Person;

@Repository
public interface PersonRepository extends CrudRepository<Person, Integer> {

}
