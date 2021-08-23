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
public class ClienteBFF implements HealthIndicator {

	Logger logger = LoggerFactory.getLogger(ClienteBFF.class);

@Value("${cliente-rest.url}")
private String urlClienteRest; 
	
private Cliente cliente = new Cliente();

@Override
public Health health() {
	logger.debug("[health] ClienteBFF");
	int ponto=0;
	try
	{	
		logger.debug("Criando cliente sintético para enviar ao BFF para cadastro");
		cliente.setCpf(123L);
		cliente.setNome("CLIENTE SINTETICO - HEALTH CHECK");
		cliente.setNumero(10);
		cliente.setNasc(new java.util.Date());
		cliente.setCidade("Cidade 1");
		cliente.setComplemento(" ");
		cliente.setLogradouro("Rua teste");
		cliente.setMae("Mae teste");
		cliente.setUf("SP");
		cliente.setCep("123442");

		RestTemplate clienteRest = new RestTemplate();
		logger.debug("vai enviar o cliente sintetico ao BFF para cadastro");
		clienteRest.postForObject(urlClienteRest,cliente, RetornoCliente.class);
		logger.debug("Solictacao de cadastro efetuada com sucesso, aguardando 200ms para conclusao antes de seguir com o HealthCheck");
		ponto=1;
		//aguarda o processamento asincrino
		Thread.sleep(400);
		logger.debug("Solicitando ao BFF a exclusao do cliente sintético que acabou de ser criado");
		clienteRest.delete(urlClienteRest + "/" + cliente.getCpf());
		logger.debug("Solicitação de exclusão do cliente sintético efetuada com sucesso");
		ponto=2;
		logger.debug("RestAPI ClienteBFF Saudável!");
		return Health.up().build();
	}
	catch (Exception e)
	{
		String mensagem = "Falha na inclusão de novos clientes: " + e.getMessage();
		if (ponto >1)
		{
			mensagem = "Falha na exclusão de um cliente: " + e.getMessage();
		}
		logger.error("Falha ao validar a saúde da restAPI ClienteBFF, " + e.getMessage(), e);
		return Health.down().withDetail("Cliente-BFF Não saudável", mensagem).build();
	}
	
}
}
