package com.labmanagement.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.labmanagement.dao.UserDAO;
import com.labmanagement.entity.User;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserDAO userDAO;

	public JwtAuthenticationFilter(JwtService jwtService, UserDAO userDAO) {
		this.jwtService = jwtService;
		this.userDAO = userDAO;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String header = request.getHeader("Authorization");

		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = header.substring(7);

		try {
			String email = jwtService.extractEmail(token);

			if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				User user = userDAO.findByEmailIgnoreCase(email).orElse(null);

				if (user != null && user.getActiveFlag() == 1 && jwtService.isTokenValid(token, email)) {

					var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

					var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);

					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}

		} catch (JwtException | IllegalArgumentException exception) {
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}
}