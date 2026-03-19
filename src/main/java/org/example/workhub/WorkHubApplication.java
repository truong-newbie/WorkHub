package org.example.workhub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.config.properties.AdminInfoProperties;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.domain.entity.Role;
import org.example.workhub.repository.RoleRepository;
import org.example.workhub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;


@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties({AdminInfoProperties.class})
@SpringBootApplication
public class WorkHubApplication {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        Environment env = SpringApplication.run(WorkHubApplication.class, args).getEnvironment();
        String appName = env.getProperty("spring.application.name");
        if (appName != null) {
            appName = appName.toUpperCase();
        }
        String port = env.getProperty("server.port");
        log.info("-------------------------START " + appName
                + " Application------------------------------");
        log.info("   Application         : " + appName);
        log.info("   Url swagger-ui      : http://localhost:" + port + "/swagger-ui.html");
        log.info("-------------------------START SUCCESS " + appName
                + " Application------------------------------");
    }

//    @Bean
//    CommandLineRunner init(AdminInfoProperties userInfo) {
//        return args -> {
//            //init role
//            if (roleRepository.count() == 0) {
//                roleRepository.save(new Role(null, RoleConstant.ADMIN, null));
//                roleRepository.save(new Role(null, RoleConstant.RECRUITER, null));
//                roleRepository.save(new Role(null, RoleConstant.CANDIDATE, null));
//            }
//            //init admin
//            if (userRepository.count() == 0) {
//                User admin = User.builder().username(userInfo.getUsername())
//                        .password(passwordEncoder.encode(userInfo.getPassword()))
//                        .firstName(userInfo.getFirstName()).lastName(userInfo.getLastName())
//                        .role(roleRepository.findByRoleName(RoleConstant.ADMIN)).build();
//                userRepository.save(admin);
//            }
//        };
//    }
}
