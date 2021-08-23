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
	int ponto=0;
	try
	{	

		RestTemplate clienteRest = new RestTemplate();
		logger.debug("vai enviar o cliente sintetico ao BFF para cadastro");
		clienteRest.getForObject(urlClienteRest + "/17956462843", RetornoCliente.class);

		logger.debug("RestAPI ClienteRest Saudável!");
		return Health.up().build();
	}
	catch (Exception e)
	{
		String mensagem = "Falha na inclusão de novos clientes: " + e.getMessage();
		if (ponto >1)
		{
			mensagem = "Falha na exclusão de um cliente: " + e.getMessage();
		}
		logger.error("Falha ao validar a saúde da restAPI ClienteRest, " + e.getMessage(), e);
		return Health.down().withDetail("Cliente-BFF Não saudável", mensagem).build();
	}
	
}
}
