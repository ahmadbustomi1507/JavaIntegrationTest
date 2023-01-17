package com.fis.app;

import com.fis.app.controller.PersonIT;
import com.fis.app.service.kafkaServiceIT;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
@Suite
@SelectClasses({
        PersonIT.class,
        kafkaServiceIT.class
})
public class TestSuite {
}
