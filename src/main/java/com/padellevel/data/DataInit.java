package com.padellevel.data;

import org.springframework.stereotype.Component;
import net.datafaker.Faker;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.padellevel.repository.UserRepository;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Component
public class DataInit implements InitializingBean {

    private final UserRepository userRepository;
    private final Faker faker;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInit(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.faker = new Faker();
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Avatar a1 = new Avatar("John Normal");
        Icon Maleicon = VaadinIcon.MALE.create();
        Icon Femaleicon = VaadinIcon.FEMALE.create();
        Avatar a2 = new Avatar("Emma Executive");

        // Create admin user 1
        if (!userRepository.existsByUsername("user")) {
            User user1 = new User();
            // user1.setId(1L);
            user1.setUsername("user");
            user1.setName("John Normal");
            user1.setApellido("Normal");
            user1.setHashedPassword(passwordEncoder.encode("user"));
            user1.setProfilePicture(Maleicon.getElement().getText().getBytes()); // truncated for brevity
            user1.setRoles(new HashSet<>(Arrays.asList(Role.USER)));
            userRepository.save(user1);
        }

        // Create admin user 2
        if (!userRepository.existsByUsername("admin")) {
            User user2 = new User();
            // user2.setId(2L);
            user2.setUsername("admin");
            user2.setName("Emma Executive");
            user2.setApellido("Executive");
            user2.setHashedPassword(passwordEncoder.encode("admin"));
            user2.setProfilePicture(Femaleicon.getElement().getText().getBytes()); // truncated for brevity
            user2.setRoles(new HashSet<>(Arrays.asList(Role.USER, Role.ADMIN)));
            userRepository.save(user2);
        }

                // Create admin user 2
        if (!userRepository.existsByUsername("admin1")) {
            User user3 = new User();
            // user3.setId(3L);
            user3.setUsername("admin1");
            user3.setName("David  Executive");
            user3.setApellido("Executive");
            user3.setHashedPassword(passwordEncoder.encode("admin"));
            user3.setProfilePicture(Femaleicon.getElement().getText().getBytes()); // truncated for brevity
            user3.setRoles(new HashSet<>(Arrays.asList(Role.PLAYER, Role.ADMIN)));
            userRepository.save(user3);
        }
        // Create 12 unique players
        Set<String> usernames = new HashSet<>();
        while (usernames.size() < 12) {
            String username = faker.name().username();
            if (!userRepository.existsByUsername(username)) {
                usernames.add(username);
            }
        }

        for (String username : usernames) {
            User player = new User();
            player.setUsername(username);
            player.setName(faker.name().firstName());
            player.setApellido(faker.name().lastName());
            player.setHashedPassword(passwordEncoder.encode("password")); // Set a default password
            player.setProfilePicture(new byte[0]); // Set a default profile picture or handle accordingly
            player.setRoles(new HashSet<>(Arrays.asList(Role.PLAYER)));
            userRepository.save(player);
        }

        // Example initialization of Torneo (if applicable)
        /*
        if (!torneoRepository.existsByNombre("Torneo Ejemplo")) {
            Torneo torneo = new Torneo();
            torneo.setNombre("Torneo Ejemplo");
            torneo.setTipo(TipoTorneo.ROUND_ROBIN);
            torneo.setNumeroEnfrentamientos(3);
            torneo.setJuegosPorEnfrentamiento(2);
            torneo.setNumeroEnfrentamientosSimultaneos(1);
            torneo.setNumeroDeVueltas(2); // Set numeroDeVueltas
            torneo.setJugadores(new ArrayList<>(userRepository.findAll()));
            torneoRepository.save(torneo);
        }
        */
    }
}