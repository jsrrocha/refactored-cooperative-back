package com.challenge.cooperative.model.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "Agenda")
public class Agenda {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	 
	@OneToMany(mappedBy = "agenda",cascade = CascadeType.ALL)  
	private List<Voting> voting;   

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Voting> getVoting() {
		return voting;
	}

	public void setVoting(List<Voting> voting) {
		this.voting = voting;
	}
	
}
