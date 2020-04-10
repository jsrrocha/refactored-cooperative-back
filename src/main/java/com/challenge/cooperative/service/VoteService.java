package com.challenge.cooperative.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.challenge.cooperative.model.entities.Associate;
import com.challenge.cooperative.model.entities.Vote;
import com.challenge.cooperative.model.entities.Voting;
import com.challenge.cooperative.repository.VoteRepository;

public class VoteService {
	@Autowired 
	private VoteRepository voteRepository;
	
	public Vote saveVote(boolean option, Associate associate, Voting voting) {
		Vote vote =  new Vote();
		vote.setVote(option); 
		vote.setAssociate(associate);
		vote.setVoting(voting);
		voteRepository.save(vote); 
		return vote;
	}
}
