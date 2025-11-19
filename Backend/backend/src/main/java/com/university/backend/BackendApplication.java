package com.university.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner testDbConnection(DataSource dataSource) {
		return args -> {
			try (Connection conn = dataSource.getConnection()) {
				System.out.println("DB Connection SUCCESS! Catalog is: " + conn.getCatalog());
			} catch (Exception ex) {
				System.err.println("DB Connection FAILURE: " + ex.getMessage());
			}
		};
	}

}
