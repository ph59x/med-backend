package xyz.ph59.med.filter;

import com.alibaba.fastjson2.JSON;
import org.springframework.http.HttpStatus;
import xyz.ph59.med.entity.Result;
import xyz.ph59.med.util.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getServletPath().startsWith("/auth/register")  ||
                request.getServletPath().startsWith("/auth/login"))  {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            sendErrorResponse(response);
            return;
        }

        DecodedJWT jwt = jwtUtil.verifyToken(header.substring(7));
        if (jwt == null) {
            sendErrorResponse(response);
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        Integer.parseInt(jwt.getSubject()),
                        null,
                        Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + jwt.getClaim("role").asString()))
                )
        );
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSON.toJSONString(
                    Result.builder(HttpStatus.UNAUTHORIZED)
                            .message("Invalid access token.")
                            .build()
            ));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        SecurityContextHolder.clearContext();
    }
}