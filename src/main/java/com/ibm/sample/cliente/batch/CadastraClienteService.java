package com.ibm.sample.cliente.batch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ibm.sample.cliente.batch.dto.RetornoCliente;
import com.ibm.sample.cliente.bff.dto.Cliente;

@Service
public class CadastraClienteService {

	
	@Value("${cliente-rest.url}")
	private String urlClienteRest; 
	
	@Value("${cliente-kafka-topico}")
	private String cadastroTopic; 
	
	@KafkaListener(topics = "clientes")
	public void cadastraCliente(Cliente cliente)
	{
		RestTemplate clienteRest = new RestTemplate();
		RetornoCliente retorno = clienteRest.postForObject(urlClienteRest,cliente, RetornoCliente.class);
		//System.out.println("Resultado " + retorno.getMensagem());
	}
}
