package com.verve.guard.serviceImpl;

import com.verve.guard.entity.AccountManagement;
import com.verve.guard.entity.Role;
import com.verve.guard.entity.User;
import com.verve.guard.enums.TransactionType;
import com.verve.guard.enums.RoleName;
import com.verve.guard.exception.InvalidPasswordException;
import com.verve.guard.mapper.UserMapper;
import com.verve.guard.repository.AccountManagementRepository;
import com.verve.guard.repository.RoleRepository;
import com.verve.guard.repository.UserRepository;
import com.verve.guard.request.LoginRequest;
import com.verve.guard.request.UserRequest;
import com.verve.guard.response.LoginResponse;
import com.verve.guard.security.JwtProvider;
import com.verve.guard.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final AccountManagementRepository managementRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    //
    private static final String CURRENCY_VALUE = "NGN";
    private static final BigDecimal DEFAULT_BALANCE = BigDecimal.ZERO;


    public AuthServiceImpl(AccountManagementRepository managementRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           RoleRepository roleRepository,
                           JwtProvider jwtProvider,
                           AuthenticationManager authenticationManager,
                           UserMapper userMapper
                           ) {
        this.managementRepository = managementRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.jwtProvider = jwtProvider;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!matches){
            System.out.println("Password does not match");
            throw new InvalidPasswordException("Bad Credentials, Try again");
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String token = jwtProvider.generateToken(user.getEmail());

        log.info("Token: {}", token);

        return LoginResponse.builder().token(token).build();
    }


    @Override
    @Transactional
    public  User register(UserRequest userRequest) {

        log.warn("REGISTER METHOD HIT >>> {}", System.currentTimeMillis());

        var user = userRepository.save(userMapper.register(userRequest));

        log.warn("USER SAVED >>> {}", user.getEmail());

        //
        Role userRole = new Role();
        userRole.setName(RoleName.ROLE_MERCHANT);
        userRole.setUser(user);

        roleRepository.save(userRole);
        log.info("Role is saved:");

        //
        AccountManagement management = new AccountManagement();
        management.setCurrency(CURRENCY_VALUE);
        management.setBalance(DEFAULT_BALANCE);
        management.setMerchantId(user.getMerchantId());
        management.setTransactionType(TransactionType.DEPOSIT);
        management.setCardNumber(generateCardNumber());
        management.setUser(user);

        managementRepository.save(management);
        log.info("Default value is stored in AccountManagement: {} ", management);

        return user;
    }

    public static long generateCardNumber(){
        SecureRandom secureRandom = new SecureRandom();
        long upperBound = 1_000_000_000_000_000L; //generates 15 secure random numbers
        return secureRandom.nextLong(upperBound);
    }


}
