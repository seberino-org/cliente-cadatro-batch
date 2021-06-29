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
	
	@KafkaListener(topics = "${cliente-kafka-topico}")
	public void cadastraCliente(Cliente cliente)
	{
		if (cliente==null || cliente.getCpf()==0L) //health check
		{
			return;
		}
		try
		{
			RestTemplate clienteRest = new RestTemplate();
			RetornoCliente retorno = clienteRest.postForObject(urlClienteRest,cliente, RetornoCliente.class);
			//System.out.println("Resultado " + retorno.getMensagem());
		}
		catch (Exception e)
		{
			System.out.println("Problemas no registro do cliente: " + e.getMessage());
		}
	}
	
	@KafkaListener(topics = "${delete-cliente-kafka-topico}")
	public void excluiCliente(Cliente cliente)
	{
		if (cliente==null || cliente.getCpf()==0L) //health check
		{
			return;
		}
		try
		{
			RestTemplate clienteRest = new RestTemplate();
			clienteRest.delete(urlClienteRest + "/" + cliente.getCpf());
			//System.out.println("Resultado ");
			
		}
		catch (Exception e)
		{
			System.out.println("Problemas na exclusao do registro do cliente: " + e.getMessage());
		}
	}
	
}
