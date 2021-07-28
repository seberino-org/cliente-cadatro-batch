package com.ibm.sample.cliente.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;

@SpringBootApplication
public class ClienteCadastroBatchApplication {


	public static void main(String[] args) {
		SpringApplication.run(ClienteCadastroBatchApplication.class, args);
	}

}
