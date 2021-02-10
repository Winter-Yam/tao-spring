package com.winter.taospring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.servlet.annotation.WebServlet;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ServletComponentScan
public class TaospringApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaospringApplication.class, args);
	}

}
