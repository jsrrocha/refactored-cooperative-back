package com.challenge.cooperative;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.challenge.cooperative.model.entities.Agenda;
import com.challenge.cooperative.model.entities.Associate;
import com.challenge.cooperative.repository.AgendaRepository;
import com.challenge.cooperative.repository.AssociateRepository;
import com.challenge.cooperative.repository.VoteRepository;
import com.challenge.cooperative.repository.VotingRepository;
import com.challenge.cooperative.service.SessionService;
import com.challenge.cooperative.util.TestUtil;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class CooperativeApplicationTests {

	@Autowired
	private AgendaRepository agendaRepository;

	@Autowired
	private AssociateRepository associateRepository;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private VotingRepository votingRepository;

	@Autowired
	private VoteRepository voteRepository;

	private TestRestTemplate restTemplate;

	private TestUtil testUtil = new TestUtil();

	@Test
	public void testAddAgenda() {
		String name = "Teste de nome de pauta";
		Map<String, String> agendaMap = new HashMap<String, String>();
		agendaMap.put("name", name);

		restTemplate = new TestRestTemplate();
		ResponseEntity<?> response = restTemplate.postForEntity("http://localhost:8086/cooperative/agenda/add",
				agendaMap, String.class);

		Agenda agenda = agendaRepository.findByName(name);

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertEquals(agenda.getName(), name);

		agendaRepository.delete(agenda);
	}

	@Test
	public void testAddAgendaWithoutName() {
		Map<String, String> agendaMap = new HashMap<String, String>();

		restTemplate = new TestRestTemplate();
		ResponseEntity<?> response = restTemplate.postForEntity("http://localhost:8086/cooperative/agenda/add",
				agendaMap, String.class);

		String responseMessage = (String) response.getBody();

		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
		assertEquals(responseMessage, "Preencha o nome da pauta");
	}

	@Test
	public void testAddAgendaWithSameName() {
		String name = "Teste de pauta com o mesmo nome";
		Agenda agenda = new Agenda();
		agenda.setName(name);
		agendaRepository.save(agenda);

		Map<String, String> agendaMap = new HashMap<String, String>();
		agendaMap.put("name", name);
		restTemplate = new TestRestTemplate();
		ResponseEntity<?> response = restTemplate.postForEntity("http://localhost:8086/cooperative/agenda/add",
				agendaMap, String.class);

		String responseMessage = (String) response.getBody();
		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
		assertEquals(responseMessage, "Já existe uma agenda com esse nome");

		agendaRepository.delete(agenda);
	}

	@Test
	public void testGetAgendas() {
		restTemplate = new TestRestTemplate();
		ResponseEntity<?> response = restTemplate.getForEntity("http://localhost:8086/cooperative/agenda/",
				Iterable.class);
		Iterable<?> agendasResponse = (Iterable<?>) response.getBody();

		Iterable<?> agendas = agendaRepository.findAll();
		assertEquals(Lists.newArrayList(agendasResponse).size(), Lists.newArrayList(agendas).size());
	}

	@Test
	public void testOpenAgendaVotingSession() {
		Agenda agenda = testUtil.createAgendaToTest(agendaRepository);

		restTemplate = new TestRestTemplate();
		ResponseEntity<?> response = restTemplate.exchange(
				"http://localhost:8086/cooperative/agenda/{id}/voting/session/open/{time}", HttpMethod.POST, null,
				String.class, agenda.getId(), 1);

		String responseMessage = (String) response.getBody();

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertEquals(responseMessage, null);

		// Clean DB
		sessionService.deleteSessionAttributeAgendaId(agenda.getId().toString());
		testUtil.deleteVotingAgendaToTest(agenda, votingRepository, voteRepository);
		testUtil.deleteAssociateToTest(associateRepository);
	}

	@Test
	public void testOpenAgendaVotingSessionAgendaNotExists() {
		TestRestTemplate restTemplate = new TestRestTemplate();
		ResponseEntity<?> response = restTemplate.exchange(
				"http://localhost:8086/cooperative/agenda/{id}/voting/session/open/{time}", HttpMethod.POST, null,
				String.class, 5000, 1);

		String responseMessage = (String) response.getBody();

		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
		assertEquals(responseMessage, "Pauta não existe");
	}

	@Test
	public void testOpenAgendaVotingSessionVotingWithTimeZero() {
		Agenda agenda = testUtil.createAgendaToTest(agendaRepository);

		TestRestTemplate restTemplate = new TestRestTemplate();
		ResponseEntity<?> response = restTemplate.exchange(
				"http://localhost:8086/cooperative/agenda/{id}/voting/session/open/{time}", HttpMethod.POST, null,
				String.class, agenda.getId(), 0);

		String responseMessage = (String) response.getBody();

		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
		assertEquals(responseMessage, "Preencha o tempo da sessão com um valor maior que 0");
	}
	

	@Test
	public void testOpenAgendaVotingSessionVotingAlreadyExists() {
		Agenda agenda = testUtil.createAgendaToTest(agendaRepository);

		restTemplate = new TestRestTemplate();
		ResponseEntity<?> response = restTemplate.exchange(
				"http://localhost:8086/cooperative/agenda/{id}/voting/session/open/{time}", HttpMethod.POST, null,
				String.class, agenda.getId(), 1);
		assertEquals(response.getStatusCode(), HttpStatus.OK);

		restTemplate = new TestRestTemplate();
		ResponseEntity<?> responseTryOpenAnotherSession = restTemplate.exchange(
				"http://localhost:8086/cooperative/agenda/{id}/voting/session/open/{time}", HttpMethod.POST, null,
				String.class, agenda.getId(), 1);

		String responseMessage = (String) responseTryOpenAnotherSession.getBody();

		assertEquals(responseTryOpenAnotherSession.getStatusCode(), HttpStatus.BAD_REQUEST);
		assertEquals(responseMessage, "Já existe uma votação aberta para essa pauta");

		// Clean DB
		sessionService.deleteSessionAttributeAgendaId(agenda.getId().toString());
		testUtil.deleteVotingAgendaToTest(agenda, votingRepository, voteRepository);
	}
	
	@Test
	public void testAgendaVoting() {
		Agenda agenda = testUtil.createAgendaToTest(agendaRepository);
		Associate associate = testUtil.createAssociateToTest(associateRepository);

		// Open session
		restTemplate = new TestRestTemplate();
		ResponseEntity<?> responseOpenSession = restTemplate.exchange(
				"http://localhost:8086/cooperative/agenda/{id}/voting/session/open/{time}", HttpMethod.POST, null,
				String.class, agenda.getId(), 1);
		assertEquals(responseOpenSession.getStatusCode(), HttpStatus.OK);
		
		// Vote on the agenda
		restTemplate = new TestRestTemplate();
		HttpEntity<?> entity = 	testUtil.buildEntity(associate.getId());
		ResponseEntity<?> response = restTemplate.exchange("http://localhost:8086/cooperative/agenda/{id}/voting",
				HttpMethod.POST, entity, String.class, agenda.getId());

		String responseMessage = (String) response.getBody();

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertEquals(responseMessage, null); 

		// Clean DB
		sessionService.deleteSessionAttributeAgendaId(agenda.getId().toString());
		testUtil.deleteVotingAgendaToTest(agenda, votingRepository, voteRepository);
		testUtil.deleteAssociateToTest(associateRepository);
	}

	@Test
	public void testAgendaVotingWithoutVote() {
		Agenda agenda = testUtil.createAgendaToTest(agendaRepository);
		Associate associate = testUtil.createAssociateToTest(associateRepository);

		Map<String, Object> votingMap = new HashMap<String, Object>();
		votingMap.put("associate", associate.getId());
		HttpEntity<?> entity = new HttpEntity<>(votingMap);

		restTemplate = new TestRestTemplate();
		ResponseEntity<?> response = restTemplate.exchange("http://localhost:8086/cooperative/agenda/{id}/voting",
				HttpMethod.POST, entity, String.class, agenda.getId());

		String responseMessage = (String) response.getBody();

		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
		assertEquals(responseMessage, "Preencha o voto");
	}

	@Test
	public void testAgendaVotingWithoutAssociate() {
		Agenda agenda = testUtil.createAgendaToTest(agendaRepository);

		Map<String, Object> votingMap = new HashMap<String, Object>();
		votingMap.put("vote", "Sim");
		HttpEntity<?> entity = new HttpEntity<>(votingMap);

		restTemplate = new TestRestTemplate();
		ResponseEntity<?> response = restTemplate.exchange("http://localhost:8086/cooperative/agenda/{id}/voting",
				HttpMethod.POST, entity, String.class, agenda.getId());

		String responseMessage = (String) response.getBody();

		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
		assertEquals(responseMessage, "Preencha o associado");
	}

	@Test
	public void testAgendaVotingWithAssociateNotExists() {
		Agenda agenda = testUtil.createAgendaToTest(agendaRepository);
		
		restTemplate = new TestRestTemplate();
		HttpEntity<?> entity = 	testUtil.buildEntity(5000l);
		ResponseEntity<?> response = restTemplate.exchange("http://localhost:8086/cooperative/agenda/{id}/voting",
				HttpMethod.POST, entity, String.class, agenda.getId());

		String responseMessage = (String) response.getBody();

		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
		assertEquals(responseMessage, "Associado não existe");
	}
	
	@Test
	public void testAgendaVotingWithAgendaNotExists() {
		Associate associate = testUtil.createAssociateToTest(associateRepository);

		HttpEntity<?> entity = 	testUtil.buildEntity(associate.getId());
		restTemplate = new TestRestTemplate();
		ResponseEntity<?> response = restTemplate.exchange("http://localhost:8086/cooperative/agenda/{id}/voting",
				HttpMethod.POST, entity, String.class, 5000);

		String responseMessage = (String) response.getBody();

		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
		assertEquals(responseMessage, "Agenda não existe");
	}
	
	@Test
	public void testAgendaVotingWithoutVoting() {
		Agenda agenda = testUtil.createAgendaToTest(agendaRepository);
		Associate associate = testUtil.createAssociateToTest(associateRepository);
		
		// Vote on the agenda
		restTemplate = new TestRestTemplate();
		HttpEntity<?> entity = 	testUtil.buildEntity(associate.getId());
		ResponseEntity<?> response = restTemplate.exchange("http://localhost:8086/cooperative/agenda/{id}/voting",
				HttpMethod.POST, entity, String.class, agenda.getId());

		String responseMessage = (String) response.getBody();

		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
		assertEquals(responseMessage, "Não existe votação para essa pauta"); 
	}

	@Test
	public void testAgendaVotingAssociateAlreadyVote() {
		Agenda agenda = testUtil.createAgendaToTest(agendaRepository);
		Associate associate = testUtil.createAssociateToTest(associateRepository);

		// Open session
		restTemplate = new TestRestTemplate();
		ResponseEntity<?> responseOpenSession = restTemplate.exchange(
				"http://localhost:8086/cooperative/agenda/{id}/voting/session/open/{time}", HttpMethod.POST, null,
				String.class, agenda.getId(), 1);
		assertEquals(responseOpenSession.getStatusCode(), HttpStatus.OK);

		// Vote one time
		restTemplate = new TestRestTemplate();
		HttpEntity<?> entity = 	testUtil.buildEntity(associate.getId());
		ResponseEntity<?> response = restTemplate.exchange("http://localhost:8086/cooperative/agenda/{id}/voting",
				HttpMethod.POST, entity, String.class, agenda.getId());

		assertEquals(response.getStatusCode(), HttpStatus.OK);

		// Try vote again
		restTemplate = new TestRestTemplate();
		ResponseEntity<?> responseWithSameAssociate = restTemplate.exchange(
				"http://localhost:8086/cooperative/agenda/{id}/voting", HttpMethod.POST, entity, String.class,
				agenda.getId());

		String responseMessage = (String) responseWithSameAssociate.getBody();

		assertEquals(responseWithSameAssociate.getStatusCode(), HttpStatus.BAD_REQUEST);
		assertEquals(responseMessage, "Associado já votou nessa pauta");

		// Clean DB
		sessionService.deleteSessionAttributeAgendaId(agenda.getId().toString());
		testUtil.deleteVotingAgendaToTest(agenda, votingRepository, voteRepository);
		testUtil.deleteAssociateToTest(associateRepository);
	}
}
