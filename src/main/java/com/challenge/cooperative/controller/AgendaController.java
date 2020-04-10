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
import com.challenge.cooperative.model.entities.Associate;
import com.challenge.cooperative.model.entities.Vote;
import com.challenge.cooperative.model.entities.Voting;
import com.challenge.cooperative.service.AgendaService;
import com.challenge.cooperative.service.AssociateService;
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
	private AssociateService associateService; 
	
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
			return new ResponseEntity<AgendaDTO>(agendaDTO,HttpStatus.OK);   
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST); 
		} 
	}

	@GetMapping("/")
	public ResponseEntity<?> getAgendas(){
		try {
			Iterable<Agenda> agendas = agendaService.getAllAgendas();
			return new ResponseEntity<>(agendas, HttpStatus.OK);  
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); 
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

			return new ResponseEntity<>(HttpStatus.OK);
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); 
		}
	}

    @Transactional 
	@PostMapping("/{id}/voting")
	public ResponseEntity<?> AgendaVoting(@PathVariable Long id, @RequestBody VoteDTO voteDTO){
		try {
			String errorMessage = agendaUtil.validateVotingInput(id, voteDTO);
			if(errorMessage != null) {
				return new ResponseEntity<String>(errorMessage, HttpStatus.BAD_REQUEST); 
			}
			Agenda agenda = agendaService.getAgendaById(id);			
			List<Voting> votingList = votingService.getVotingByAgenda(agenda);
			
			boolean option = voteMap.get("vote").toString().equals("Sim") ? true : false;
			Associate associate = associateService.getAssociateById(Long.parseLong(voteMap.get("associate").toString()));
			Vote vote = voteService.saveVote(option, associate, votingList.get(0));
			
			return new ResponseEntity<Vote>(vote,HttpStatus.OK);
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); 
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
			
    		List<Vote> votes = votingList.get(0).getVotes();
            Long optionYes = votes.stream().filter(vote-> vote.isVote() == true).count();
            Long optionNo = votes.stream().filter(vote-> vote.isVote() == false).count(); 

            String result =  agendaUtil.getResult(optionYes, optionNo);
    		
    		Map<String,String> response = agendaUtil.buildResponseMap(optionYes, optionNo, result); 
			return new ResponseEntity<Map<String,String>>(response,HttpStatus.OK);
			
    	}catch (Exception e) {
    		e.printStackTrace();
    		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); 
    	}
    }
}
