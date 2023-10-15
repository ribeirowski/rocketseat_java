package com.ribeirowski.todolist.filter;

import java.io.IOException;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.ribeirowski.todolist.user.UserRepository;
import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var servletPath = request.getServletPath();

        if (servletPath.startsWith("/task")) {
            // Get authentication (username and password)
            var authorization = request.getHeader("Authorization");
            // Get only the encoded credentials in Base64 format
            var authEncoded = authorization.substring("Basic ".length());
            // Get the decoded credentials in byte array format
            byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
            // Get the decoded credentials in string format
            var authString = new String(authDecoded);
            // Split the username and password into username and password
            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];

            // Validate the username
            var user = this.userRepository.findByUsername(username);
            if (user == null) {
                response.sendError(401);
                return;
            } else {
                // Validate the password
                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if (passwordVerify.verified) {
                    request.setAttribute("idUser", user.getId());
                    // If the authentication is valid, continue the request
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                    return;
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

}
