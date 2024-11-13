package com.scalableServices.customerMicroservice;

import com.scalableServices.customerMicroservice.config.CustomUserDetailsService;
import com.scalableServices.customerMicroservice.config.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final AuthenticationManager authenticationManager;
    private final CustomerRepository customerRepository;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;
    private final RestaurantServiceClient restaurantServiceClient;

    @Autowired
    public CustomerController(
            AuthenticationManager authenticationManager,
            CustomerRepository customerRepository,
            CustomUserDetailsService userDetailsService,
            JwtTokenUtil jwtTokenUtil,
            PasswordEncoder passwordEncoder,
            RestaurantServiceClient restaurantServiceClient) {
        this.authenticationManager = authenticationManager;
        this.customerRepository = customerRepository;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.passwordEncoder = passwordEncoder;
        this.restaurantServiceClient = restaurantServiceClient;
    }

    /**
     * Register a new customer.
     *
     * @param customer Customer registration details.
     * @return Registered customer details.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Customer customer) {
        // Check if email already exists
        if (customerRepository.findByEmail(customer.getEmail()) != null) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        // Hash the password before saving
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));

        // Save the customer
        Customer newCustomer = customerRepository.save(customer);

        // Do not return the password in the response
        newCustomer.setPassword(null);

        return ResponseEntity.ok(newCustomer);
    }

    /**
     * Authenticate a customer and generate a JWT token.
     *
     * @param authRequest Authentication request containing email and password.
     * @return JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest authRequest) {
        try {
            // Authenticate the user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Incorrect email or password");
        }

        // Load user details
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());

        // Generate JWT token
        final String jwtToken = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwtToken));
    }

    /**
     * Get customer details by ID.
     * This endpoint is secured and requires a valid JWT token.
     *
     * @param id Customer ID.
     * @return Customer details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable Long id) {
        Customer customer = customerRepository.findById(id).orElse(null);

        if (customer != null) {
            // Do not include the password in the response
            customer.setPassword(null);
            return ResponseEntity.ok(customer);
        } else {
            return ResponseEntity.status(404).body("Customer not present in the Database");
        }
    }

    /**
     * Get all restaurants.
     * This endpoint is secured and requires a valid JWT token.
     *
     * @return List of restaurants.
     */
    @GetMapping("/restaurants")
    public ResponseEntity<?> getAllRestaurants(HttpServletRequest request) {
        String jwtToken = extractJwtFromRequest(request);
        if (jwtToken == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        List<Restaurant> restaurants = restaurantServiceClient.getAllRestaurants(jwtToken);
        return ResponseEntity.ok(restaurants);
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest, HttpServletRequest request) {
        String jwtToken = extractJwtFromRequest(request);
        if (jwtToken == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        return restaurantServiceClient.placeOrder(orderRequest, jwtToken);
    }

    @GetMapping("{customerId}/orders/{orderId}/status")
    public ResponseEntity<String> trackOrder(HttpServletRequest request, @PathVariable Long orderId, @PathVariable Long customerId) {
        String jwtToken = extractJwtFromRequest(request);
        if (jwtToken == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        return restaurantServiceClient.getOrderStatusByOrderId(customerId, orderId, jwtToken);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (org.springframework.util.StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}