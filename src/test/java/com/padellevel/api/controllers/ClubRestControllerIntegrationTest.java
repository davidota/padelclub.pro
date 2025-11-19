package com.padellevel.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.padellevel.data.Club;
import com.padellevel.data.User;
import com.padellevel.repository.ClubRepository;
import com.padellevel.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para ClubRestController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClubRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Club testClub;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        clubRepository.deleteAll();
        userRepository.deleteAll();

        // Crear usuario de prueba
        testUser = new User();
        testUser.setUsername("testadmin");
        testUser.setName("Test");
        testUser.setEmail("admin@test.com");
        testUser.setHashedPassword("hashedpassword");
        testUser = userRepository.save(testUser);

        // Crear club de prueba
        testClub = new Club();
        testClub.setNombre("Club de Prueba");
        testClub.setDireccion("Calle Test 123");
        testClub.setCiudad("Test City");
        testClub.setPais("Test Country");
        testClub.setAdministrador(testUser);
        testClub.setActivo(true);
        testClub = clubRepository.save(testClub);
    }

    @Test
    void testListarClubes_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/clubes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].nombre").value("Club de Prueba"));
    }

    @Test
    void testObtenerClub_ExistingId_ReturnsClub() throws Exception {
        mockMvc.perform(get("/api/v1/clubes/" + testClub.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nombre").value("Club de Prueba"))
                .andExpect(jsonPath("$.data.ciudad").value("Test City"))
                .andExpect(jsonPath("$.data.pais").value("Test Country"));
    }

    @Test
    void testObtenerClub_NonExistingId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/clubes/999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testBuscarClubes_ByName_ReturnsMatchingClubs() throws Exception {
        mockMvc.perform(get("/api/v1/clubes/buscar")
                .param("query", "Prueba")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].nombre", containsString("Prueba")));
    }

    @Test
    void testBuscarClubes_ByCity_ReturnsMatchingClubs() throws Exception {
        mockMvc.perform(get("/api/v1/clubes/buscar")
                .param("query", "Test City")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));
    }

    @Test
    void testObtenerEstadisticas_ReturnsStats() throws Exception {
        mockMvc.perform(get("/api/v1/clubes/" + testClub.getId() + "/estadisticas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalMiembros").isNumber())
                .andExpect(jsonPath("$.data.torneosActivos").isNumber())
                .andExpect(jsonPath("$.data.totalPistas").isNumber());
    }

    @Test
    void testBuscarClubes_NoResults_ReturnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/v1/clubes/buscar")
                .param("query", "NonExistingClubXYZ123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }
}
