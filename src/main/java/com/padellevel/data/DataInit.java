package com.padellevel.data;

import org.springframework.stereotype.Component;
import net.datafaker.Faker;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.padellevel.repository.UserRepository;
import com.padellevel.repository.LogroRepository;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Component
public class DataInit implements InitializingBean {

    private final UserRepository userRepository;
    private final LogroRepository logroRepository;
    private final Faker faker;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInit(UserRepository userRepository, LogroRepository logroRepository) {
        this.userRepository = userRepository;
        this.logroRepository = logroRepository;
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

        // Inicializar logros predefinidos
        initializeAchievements();

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

    /**
     * Inicializa logros predefinidos del sistema.
     */
    private void initializeAchievements() {
        // Logros de partidos jugados
        createLogroIfNotExists("Primera Vez", "Juega tu primer partido",
                              TipoLogro.PARTIDOS_JUGADOS, 1, "🎾");
        createLogroIfNotExists("Novato", "Juega 10 partidos",
                              TipoLogro.PARTIDOS_JUGADOS, 10, "🎾");
        createLogroIfNotExists("Aficionado", "Juega 50 partidos",
                              TipoLogro.PARTIDOS_JUGADOS, 50, "🎾");
        createLogroIfNotExists("Profesional", "Juega 100 partidos",
                              TipoLogro.PARTIDOS_JUGADOS, 100, "🎾");

        // Logros de victorias
        createLogroIfNotExists("Primera Sangre", "Gana tu primer partido",
                              TipoLogro.PRIMERA_VICTORIA, 1, "🌟");
        createLogroIfNotExists("Ganador", "Gana 10 partidos",
                              TipoLogro.VICTORIAS, 10, "🏆");
        createLogroIfNotExists("Campeón", "Gana 50 partidos",
                              TipoLogro.VICTORIAS, 50, "🏆");
        createLogroIfNotExists("Leyenda", "Gana 100 partidos",
                              TipoLogro.VICTORIAS, 100, "🏆");

        // Logros de racha de victorias
        createLogroIfNotExists("Racha Iniciada", "Gana 3 partidos consecutivos",
                              TipoLogro.RACHA_VICTORIAS, 3, "🔥");
        createLogroIfNotExists("Imparable", "Gana 5 partidos consecutivos",
                              TipoLogro.RACHA_VICTORIAS, 5, "🔥");
        createLogroIfNotExists("Dominación", "Gana 10 partidos consecutivos",
                              TipoLogro.RACHA_VICTORIAS, 10, "🔥");

        // Logros de torneos ganados
        createLogroIfNotExists("Podio Debut", "Llega al podio por primera vez",
                              TipoLogro.PRIMERA_MEDALLA, 1, "🥇");
        createLogroIfNotExists("Rey del Torneo", "Gana tu primer torneo",
                              TipoLogro.TORNEOS_GANADOS, 1, "👑");
        createLogroIfNotExists("Coleccionista de Copas", "Gana 5 torneos",
                              TipoLogro.TORNEOS_GANADOS, 5, "👑");
        createLogroIfNotExists("Dominador", "Gana 10 torneos",
                              TipoLogro.TORNEOS_GANADOS, 10, "👑");

        // Logros de participación
        createLogroIfNotExists("Debut en Torneo", "Participa en tu primer torneo",
                              TipoLogro.DEBUT, 1, "🎬");
        createLogroIfNotExists("Entusiasta", "Participa en 5 torneos",
                              TipoLogro.COLABORADOR, 5, "🤝");
        createLogroIfNotExists("Jugador Activo", "Participa en 25 torneos",
                              TipoLogro.COLABORADOR, 25, "🤝");
        createLogroIfNotExists("Alma del Club", "Participa en 50 torneos",
                              TipoLogro.COLABORADOR, 50, "🤝");

        // Logros de nivel
        createLogroIfNotExists("Aprendiz", "Alcanza el nivel 10",
                              TipoLogro.VETERANO, 10, "👴");
        createLogroIfNotExists("Experto", "Alcanza el nivel 25",
                              TipoLogro.VETERANO, 25, "👴");
        createLogroIfNotExists("Maestro", "Alcanza el nivel 50",
                              TipoLogro.VETERANO, 50, "👴");
        createLogroIfNotExists("Gran Maestro", "Alcanza el nivel 100",
                              TipoLogro.VETERANO, 100, "👴");
    }

    /**
     * Crea un logro si no existe.
     */
    private void createLogroIfNotExists(String nombre, String descripcion,
                                       TipoLogro tipo, Integer meta, String icono) {
        if (logroRepository.findAll().stream()
            .noneMatch(l -> l.getNombre().equals(nombre))) {
            Logro logro = new Logro();
            logro.setNombre(nombre);
            logro.setDescripcion(descripcion);
            logro.setTipo(tipo);
            logro.setMeta(meta);
            logro.setIcono(icono);
            logroRepository.save(logro);
        }
    }
}