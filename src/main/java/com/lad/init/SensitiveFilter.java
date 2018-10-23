package com.lad.init;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

public class SensitiveFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		SensitiveReponseWrapper wrapper = new SensitiveReponseWrapper(response);

		filterChain.doFilter(request, wrapper);
		OutputStream out = response.getOutputStream();
		byte[] content = wrapper.getContent();
		out.write(content);
	}
}