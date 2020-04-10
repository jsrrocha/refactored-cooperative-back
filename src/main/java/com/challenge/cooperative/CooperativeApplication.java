package com.challenge.cooperative;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.challenge.cooperative.service.AssociateService;

@SpringBootApplication
public class CooperativeApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = SpringApplication.run(CooperativeApplication.class, args);
		AssociateService service = applicationContext.getBean(AssociateService.class);
        service.createAssociatesToInterface(); 
	}

}
