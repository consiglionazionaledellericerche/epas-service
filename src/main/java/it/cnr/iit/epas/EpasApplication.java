package it.cnr.iit.epas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = "it.cnr.iit.epas.models")
@EnableJpaRepositories
@SpringBootApplication
public class EpasApplication {

	public static void main(String[] args) {
		SpringApplication.run(EpasApplication.class, args);
	}

}
