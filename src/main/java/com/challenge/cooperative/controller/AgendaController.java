package com.challenge.cooperative.controller;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.challenge.cooperative.model.dtos.AgendaDTO;
import com.challenge.cooperative.model.dtos.VoteDTO;
import com.challenge.cooperative.model.entities.Agenda;
import com.challenge.cooperative.model.entities.Vote;
import com.challenge.cooperative.model.entities.Voting;
import com.challenge.cooperative.service.AgendaService;
import com.challenge.cooperative.service.VoteService;
import com.challenge.cooperative.service.VotingService;
import com.challenge.cooperative.util.AgendaUtil;
import com.challenge.cooperative.util.SessionUtil;

@RestController 
@RequestMapping("agenda")
public class AgendaController { 
	
	@Autowired 
	private AgendaService agendaService;
	
	@Autowired
	private VotingService votingService; 
	
	@Autowired
	private VoteService voteService;
	
	@Autowired
	private AgendaUtil agendaUtil;
	
	@Autowired
	private SessionUtil sessionUtil; 
	

	@PostMapping("/add")
	public ResponseEntity<?> addAgenda(@RequestBody AgendaDTO agendaDTO){
		try {
			String errorMessage = agendaUtil.validateAgendaDTO(agendaDTO);
			if(errorMessage != null) {
				return new ResponseEntity<String>(errorMessage, HttpStatus.BAD_REQUEST); 
			}
			
			Agenda agenda =  agendaUtil.convertToEntity(agendaDTO);
			agenda = agendaService.saveAgenda(agenda);
			agendaDTO = agendaUtil.convertToDTO(agenda);
			return new ResponseEntity<AgendaDTO>(agendaDTO,HttpStatus.CREATED);   
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); 
		} 
	}

	@GetMapping("/")
	public ResponseEntity<?> getAllAgendas(){
		try {
			Iterable<Agenda> agendas = agendaService.getAllAgendas();
			List<AgendaDTO> agendasDTO = agendaUtil.convertListToDTO(agendas);
			
			return new ResponseEntity<List<AgendaDTO>>(agendasDTO, HttpStatus.OK);  
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); 
		}
	}  

	@Transactional 
	@PostMapping("/{id}/voting/session/open/{time}")
	public ResponseEntity<?> openAgendaVotingSession(@PathVariable Long id, @PathVariable Integer time, HttpSession session){
		try {
			String errorMessage = agendaUtil.validateOpenSessionInput(id, time);
			if(errorMessage != null) {
				return new ResponseEntity<String>(errorMessage, HttpStatus.BAD_REQUEST); 
			}

			Agenda agenda = agendaService.getAgendaById(id);
			String name = "Votação da pauta " + id.toString();
			votingService.saveVoting(name, agenda); 
			
			sessionUtil.setSession(session, id.toString(), time);

			return new ResponseEntity<>(HttpStatus.CREATED);
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); 
		}
	}

    @Transactional 
	@PostMapping("/voting")
	public ResponseEntity<?> AgendaVoting(@RequestBody VoteDTO voteDTO){
		try {
			String errorMessage = agendaUtil.validateVote(voteDTO);
			if(errorMessage != null) {
				return new ResponseEntity<String>(errorMessage, HttpStatus.BAD_REQUEST); 
			}
			
			Vote vote = voteService.convertToEntity(voteDTO);
			vote = voteService.saveVote(vote);
			
			return new ResponseEntity<>(HttpStatus.CREATED);
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); 
		}
	}
  
    @Transactional
    @PostMapping("/{id}/voting/result") 
    public ResponseEntity<?> resultVotingAgenda(@PathVariable Long id){
    	try {
    		String errorMessage = agendaUtil.validateResultInput(id);
			if(errorMessage != null) {
				return new ResponseEntity<String>(errorMessage, HttpStatus.BAD_REQUEST); 
			}
			
			Agenda agenda = agendaService.getAgendaById(id);			
			List<Voting> votingList = votingService.getVotingByAgenda(agenda);
		
            String result =  agendaUtil.getResult(votingList.get(0).getVotes());
    		Map<String,String> response = agendaUtil.buildResponseMap(result); 
    		
			return new ResponseEntity<Map<String,String>>(response,HttpStatus.OK);
    	}catch (Exception e) {
    		e.printStackTrace();
    		return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); 
    	}
    }
}
