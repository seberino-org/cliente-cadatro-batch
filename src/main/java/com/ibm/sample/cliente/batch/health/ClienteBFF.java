package com.ibm.sample.cliente.batch.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ibm.sample.cliente.batch.dto.RetornoCliente;
import com.ibm.sample.cliente.bff.dto.Cliente;

@Component
public class ClienteBFF implements HealthIndicator {

@Value("${cliente-rest.url}")
private String urlClienteRest; 
	
private Cliente cliente = new Cliente();

@Override
public Health health() {
	int ponto=0;
	try
	{
	
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
		RetornoCliente retorno = clienteRest.postForObject(urlClienteRest,cliente, RetornoCliente.class);
		ponto=1;
		//aguarda o processamento asincrino
		Thread.sleep(200);
		clienteRest.delete(urlClienteRest + "/" + cliente.getCpf());
		ponto=2;
		return Health.up().build();
	}
	catch (Exception e)
	{
		e.printStackTrace();
		String mensagem = "Falha na inclusão de novos clientes: " + e.getMessage();
		if (ponto >1)
		{
			mensagem = "Falha na exclusão de um cliente: " + e.getMessage();
		}
		return Health.down().withDetail("Cliente-BFF Não saudável", mensagem).build();
	}
	
}
}
