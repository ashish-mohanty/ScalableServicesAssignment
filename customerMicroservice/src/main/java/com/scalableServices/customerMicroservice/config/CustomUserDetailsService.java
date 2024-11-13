package com.scalableServices.customerMicroservice.config;


import com.scalableServices.customerMicroservice.Customer;
import com.scalableServices.customerMicroservice.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(email);

        if (customer == null) {
            throw new UsernameNotFoundException("Customer not found with email: " + email);
        }

        return new User(customer.getEmail(), customer.getPassword(), Collections.emptyList());
    }
}
