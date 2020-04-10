package com.challenge.cooperative.util;

import javax.servlet.http.HttpSession;

public class SessionUtil {

	public HttpSession setSession(HttpSession session, String id, Integer time) {
		session.setAttribute("agendaId",id);
		session.setMaxInactiveInterval(time * 60);
		return session;
	}
}
