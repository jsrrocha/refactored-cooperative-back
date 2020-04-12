package com.challenge.cooperative.util;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class SessionUtil {

	public HttpSession setSession(HttpSession session, String id, Integer time) {
		session.setAttribute("agendaId",id);
		session.setMaxInactiveInterval(time * 60);
		return session;
	}
	
	public boolean isAgendaSessionOpen(List<Map<String, String>> sessionAttributes, String agendaId) {
		boolean found = false;
		if (sessionAttributes != null) {
			found = sessionAttributes.stream()
					.filter(sessionAttribute -> sessionAttribute.get("attribute").equals(agendaId)).count() > 0;
		}
		return found;
	}
}
