/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ensp.agem.rest;

import com.ensp.agem.dao.RoleRepository;
import com.ensp.agem.dao.UtilisateurRepository;
import com.ensp.agem.data.Role;
import com.ensp.agem.data.Utilisateur;
import com.ensp.agem.payload.request.LoginRequest;
import com.ensp.agem.payload.response.JwtResponse;
import com.ensp.agem.security.services.UserDetailsImpl;
import com.ensp.agem.security.services.jwt.JwtUtils;
import com.ensp.agem.payload.response.MessageResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author mansour
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UtilisateurRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

            Authentication authentication = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();		
            List<String> roles = userDetails.getAuthorities().stream()
                            .map(item -> item.getAuthority())
                            .collect(Collectors.toList());

            return ResponseEntity.ok(new JwtResponse(jwt, 
                    userDetails.getId(), 
                    userDetails.getUsername(), 
                    userDetails.getEmail(), 
                    roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody Utilisateur utilisateur) {
        if (userRepository.existsByUsername(utilisateur.getUsername())) {
                    return ResponseEntity
                                    .badRequest()
                                    .body(new MessageResponse("Error: Username is already taken!"));
            }

            if (userRepository.existsByEmail(utilisateur.getEmail())) {
                    return ResponseEntity
                                    .badRequest()
                                    .body(new MessageResponse("Error: Email is already in use!"));
            }

            // Create new user's account
            Utilisateur user = new Utilisateur(utilisateur.getUsername(), 
                                                     utilisateur.getEmail(),
                                                     encoder.encode(utilisateur.getPassword()));

            List<Role> strRoles = utilisateur.getRoles();
            System.out.println(utilisateur.getRoles());
            Set<Role> roles = new HashSet<>();
             System.out.println(user.toString());
            user.setActive(1);
            user.setRoles(strRoles);
           System.out.println(user.toString());
            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
