package com.ibm.sample.cliente.batch.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ibm.sample.cliente.batch.dto.RetornoCliente;
import com.ibm.sample.cliente.bff.dto.Cliente;

@Component
public class ClienteRest implements HealthIndicator {

	Logger logger = LoggerFactory.getLogger(ClienteRest.class);

@Value("${cliente-rest.url}")
private String urlClienteRest; 
	
private Cliente cliente = new Cliente();

@Override
public Health health() {
	logger.debug("[health] ClienteBFF");
	try
	{	

		RestTemplate clienteRest = new RestTemplate();
		logger.debug("It will test a customer search using the RestAPI");
		clienteRest.getForObject(urlClienteRest + "/17956462843", RetornoCliente.class);

		logger.debug("RestAPI ClientRest Health!");
		return Health.up().build();
	}
	catch (Exception e)
	{
		String mensagem = "Error to invoke a search in the RestAPI: " + e.getMessage();
		logger.error(mensagem, e);
		return Health.down().withDetail("Cliente-Rest is not Health", mensagem).build();
	}
	
}
}
