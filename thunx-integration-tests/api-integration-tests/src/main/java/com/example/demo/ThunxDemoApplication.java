package com.example.demo;

import com.example.demo.model.Company;
import com.example.demo.repository.CompanyRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@SpringBootApplication
public class ThunxDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThunxDemoApplication.class, args);
	}

}

@Component
class ControllerCustomization implements RepositoryRestConfigurer {

	@Override
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry corsRegistry) {

		config.withEntityLookup().
				forRepository(CompanyRepository.class, Company::getVat, CompanyRepository::findByVat);
	}
}

