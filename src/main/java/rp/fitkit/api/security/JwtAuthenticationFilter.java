//package rp.fitkit.api.security;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//import rp.fitkit.api.model.User;
//import rp.fitkit.api.repository.UserRepository;
//import rp.fitkit.api.util.JwtUtil;
//
//import java.io.IOException;
//
//@Component
//@Slf4j
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    @Autowired
//    private UserRepository userRepository; // Gebruik de repository direct
//
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain
//    ) throws ServletException, IOException {
//        try {
//            String jwt = parseJwt(request);
//            if (jwt != null && jwtUtil.validateToken(jwt)) {
//                String username = jwtUtil.getUsernameFromToken(jwt);
//
//                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//
//                    User user = userRepository.findByUsername(username).block();
//
//                    UserDetails userDetails = (UserDetails) user;
//
//                    if (userDetails != null) {
//
//                        UsernamePasswordAuthenticationToken authentication =
//                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//
//                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//                        SecurityContextHolder.getContext().setAuthentication(authentication);
//                        log.debug("User '{}' geauthenticeerd via JWT. SecurityContext gezet.", username);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("Kan geen user authenticatie zetten: {}", e.getMessage());
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private String parseJwt(HttpServletRequest request) {
//        String headerAuth = request.getHeader("Authorization");
//
//        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
//            return headerAuth.substring(7);
//        }
//        return null;
//    }
//}
