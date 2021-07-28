package com.ibm.sample.cliente.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

import java.net.URI;
import java.util.HashMap;

import com.ibm.sample.HttpHeaderInjectAdapter;
import com.ibm.sample.PropagacaoContexto;
import com.ibm.sample.cliente.batch.dto.RetornoCliente;
import com.ibm.sample.cliente.bff.dto.Cliente;

@Service
public class CadastraClienteService extends PropagacaoContexto {

	Logger logger = LoggerFactory.getLogger(CadastraClienteService.class);

	@Value("${cliente-rest.url}")
	private String urlClienteRest; 
	
	@Autowired
	Tracer tracer;

	@KafkaListener(topics = "${cliente-kafka-topico}")
	public void cadastraCliente(Cliente cliente,  @Headers MessageHeaders headers)
	{
		
		logger.debug("[cadastraCliente] " + cliente);
		
		if (cliente==null || cliente.getCpf()==0L) //health check
		{
			logger.debug("CPF 0, solicitação sintética feita pelo Health Check de cadatro de cliente, por tanto será descartada ");
			return;
		}
		Span span = this.startConsumerSpan("consomeMensagemCadastroCliente", headers, tracer);
		try
		{
			span.setTag("payload", cliente.toString());
			RestTemplate clienteRest = new RestTemplate();
			logger.debug("Vai chamar a RestAPI para solicitar a gravação do cliente na base de dados");
			HttpHeaders httpHeaders = new HttpHeaders();
			HttpHeaderInjectAdapter h1 = new HttpHeaderInjectAdapter(httpHeaders);
			tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS,h1);
			
			HttpEntity<Cliente> entity = new HttpEntity<>(cliente, h1.getHeaders());
			RetornoCliente retorno = clienteRest.postForObject(urlClienteRest,entity, RetornoCliente.class);
			logger.info("Retorno da solicitação de cadastro do cliente: " + retorno.getMensagem() + ", cliente: " + retorno.getCliente().toString());
			//System.out.println("Resultado " + retorno.getMensagem());
		}
		catch (Exception e)
		{
			span.log("Error: " + e.getMessage() );
			span.setTag("error",true);
			logger.error("Falha ao gravar os dados desse cliente: " + cliente.toString() + ", erro: " + e.getMessage(), e);
		}
		finally{
			span.finish();
		}
	}
	
	@KafkaListener(topics = "${delete-cliente-kafka-topico}", groupId = "Delete-Cliente")
	public void excluiCliente(Cliente cliente,  @Headers MessageHeaders headers)
	{
		logger.debug("[excluiCliente] " + cliente);
		
		if (cliente==null || cliente.getCpf()==0L) //health check
		{
			logger.debug("CPF 0, solicitação sintética feita pelo Health Check de exclusao de cliente, por tanto será descartada ");
			return;
		}
		Span span = this.startConsumerSpan("consomeMensagemExclusaoCliente", headers, tracer);
		try
		{
			HttpHeaders httpHeaders = new HttpHeaders();
			HttpHeaderInjectAdapter h1 = new HttpHeaderInjectAdapter(httpHeaders);
			tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS,h1);
			
			HttpEntity<String> entity = new HttpEntity<>( h1.getHeaders());
			RestTemplate clienteRest = new RestTemplate();
			logger.debug("Vai chamar a RestAPI para solicitar a exclusao do cliente na base de dados");
			URI url = new URI(urlClienteRest + "/" + cliente.getCpf());
			clienteRest.exchange(url,HttpMethod.DELETE,entity,String.class);
			//clienteRest.
			logger.debug("Solicitacao de exclusao feita com sucesso para a ClienteRestAPI");
			
		}
		catch (Exception e)
		{
			span.log("Error: " + e.getMessage() );
			span.setTag("error",true);
			logger.error("Falha ao solicitar a exclusao dos dados desse cliente: " + cliente.toString() +", erro: " + e.getMessage() ,e);
		}
		finally {
			span.finish();
		}
	}
	
}
