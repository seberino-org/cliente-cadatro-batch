package com.ibm.sample.cliente.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.opentelemetry.api.trace.Span;

import com.ibm.sample.cliente.batch.dto.RetornoCliente;
import com.ibm.sample.cliente.bff.dto.Cliente;

@Service
public class CadastraClienteService {

	Logger logger = LoggerFactory.getLogger(CadastraClienteService.class);

	@Value("${cliente-rest.url}")
	private String urlClienteRest; 
	
	@KafkaListener(topics = "${cliente-kafka-topico}")
	public void cadastraCliente(Cliente cliente)
	{
		
		logger.debug("[cadastraCliente] " + cliente);
		
		if (cliente==null || cliente.getCpf()==0L) //health check
		{
			logger.debug("CPF 0, solicitação sintética feita pelo Health Check de cadatro de cliente, por tanto será descartada ");
			return;
		}
		try
		{
			Span.current().setAttribute("payload", cliente.toString());
			RestTemplate clienteRest = new RestTemplate();
			logger.debug("Vai chamar a RestAPI para solicitar a gravação do cliente na base de dados");
			RetornoCliente retorno = clienteRest.postForObject(urlClienteRest,cliente, RetornoCliente.class);
			logger.info("Retorno da solicitação de cadastro do cliente: " + retorno.getMensagem() + ", cliente: " + retorno.getCliente().toString());
			//System.out.println("Resultado " + retorno.getMensagem());
		}
		catch (Exception e)
		{
			Span.current().addEvent("Error: " + e.getMessage() );
			logger.error("Falha ao gravar os dados desse cliente: " + cliente.toString() + ", erro: " + e.getMessage(), e);
		}
	}
	
	@KafkaListener(topics = "${delete-cliente-kafka-topico}", groupId = "Delete-Cliente")
	public void excluiCliente(Cliente cliente)
	{
		logger.debug("[excluiCliente] " + cliente);
		if (cliente==null || cliente.getCpf()==0L) //health check
		{
			logger.debug("CPF 0, solicitação sintética feita pelo Health Check de exclusao de cliente, por tanto será descartada ");
			return;
		}
		try
		{
			RestTemplate clienteRest = new RestTemplate();
			logger.debug("Vai chamar a RestAPI para solicitar a exclusao do cliente na base de dados");
			clienteRest.delete(urlClienteRest + "/" + cliente.getCpf());
			logger.debug("Solicitacao de exclusao feita com sucesso para a ClienteRestAPI");
			
		}
		catch (Exception e)
		{
			logger.error("Falha ao solicitar a exclusao dos dados desse cliente: " + cliente.toString() +", erro: " + e.getMessage() ,e);
		}
	}
	
}
