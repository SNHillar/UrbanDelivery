package ar.urbandelivery;

import org.springframework.boot.SpringApplication;

public class TestUrbandeliveryApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(UrbandeliveryApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
