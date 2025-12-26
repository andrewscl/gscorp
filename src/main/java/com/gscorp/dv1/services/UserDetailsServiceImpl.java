package com.gscorp.dv1.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gscorp.dv1.security.SecurityUser;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.infrastructure.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername (String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> {
                            return new 
                                UsernameNotFoundException("User not found with username" 
                                    + username);
                        });
        return new SecurityUser(user);
    }

}
