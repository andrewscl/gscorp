package com.gscorp.dv1.auth;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.gscorp.dv1.security.JwtService;
import com.gscorp.dv1.services.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    
    @Override
    protected void doFilterInternal 
                    (@NonNull HttpServletRequest request,
                        @NonNull HttpServletResponse reponse,
                        @NonNull FilterChain filterChain)
                            throws ServletException, IOException {

        String path = request.getServletPath();
        if(path.startsWith("/css/") ||
           path.startsWith("/js/") ||
           path.startsWith("/img/") ||
           path.startsWith("/videos/") ||
           path.startsWith("/favicon.ico/") ||
           path.startsWith("/webjars/") ||
           path.startsWith("/icons/")) {
            filterChain.doFilter(request, reponse);
            return;
           }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        System.out.println("Solicitud entrante: " + request.getRequestURI());
        System.out.println("Authorization header: " + authHeader);
        

        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            System.out.println("No se encontrol un token valid en el header.");
            filterChain.doFilter(request, reponse);
            return;
        }

        jwt = authHeader.substring(7);
        username = jwtService.extractUsername(jwt);
        System.out.println("Usuario extraido del JWT: " + username);

        if(username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails =
                this.userDetailsServiceImpl.loadUserByUsername(username);
            
            if(jwtService.isTokenValid(jwt, userDetails)){
                System.out.println("JWT valido, autenticando usuario...");
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                System.out.println("El token no es valido para el usuario");
            }
        }
        filterChain.doFilter(request, reponse);
    }
}
