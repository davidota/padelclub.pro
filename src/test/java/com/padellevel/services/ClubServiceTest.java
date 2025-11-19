package com.padellevel.services;

import com.padellevel.data.Club;
import com.padellevel.data.User;
import com.padellevel.repository.ClubRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ClubService.
 */
@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private ClubService clubService;

    private Club testClub;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setName("Test");
        testUser.setEmail("test@test.com");

        testClub = new Club();
        testClub.setId(1L);
        testClub.setNombre("Test Club");
        testClub.setDireccion("Test Address");
        testClub.setCiudad("Test City");
        testClub.setPais("Test Country");
        testClub.setAdministrador(testUser);
        testClub.setActivo(true);
    }

    @Test
    void testFindAllActive_ReturnsActiveClubs() {
        // Arrange
        List<Club> activeClubs = Arrays.asList(testClub);
        when(clubRepository.findByActivo(true)).thenReturn(activeClubs);

        // Act
        List<Club> result = clubService.findAllActive();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Club", result.get(0).getNombre());
        verify(clubRepository, times(1)).findByActivo(true);
    }

    @Test
    void testFindById_ExistingClub_ReturnsClub() {
        // Arrange
        when(clubRepository.findById(1L)).thenReturn(Optional.of(testClub));

        // Act
        Optional<Club> result = clubService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Club", result.get().getNombre());
        verify(clubRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NonExistingClub_ReturnsEmpty() {
        // Arrange
        when(clubRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Club> result = clubService.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(clubRepository, times(1)).findById(999L);
    }

    @Test
    void testCrearClub_ValidClub_SavesClub() {
        // Arrange
        Club newClub = new Club();
        newClub.setNombre("New Club");
        newClub.setDireccion("New Address");
        newClub.setCiudad("New City");
        newClub.setPais("New Country");

        when(clubRepository.save(any(Club.class))).thenReturn(testClub);

        // Act
        Club result = clubService.crearClub(newClub, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser, newClub.getAdministrador());
        assertTrue(newClub.getActivo());
        verify(clubRepository, times(1)).save(any(Club.class));
    }

    @Test
    void testAgregarMiembro_ValidUser_AddsMember() {
        // Arrange
        when(clubRepository.findById(1L)).thenReturn(Optional.of(testClub));
        when(clubRepository.save(any(Club.class))).thenReturn(testClub);

        User newMember = new User();
        newMember.setId(2L);
        newMember.setUsername("newmember");

        // Act
        clubService.agregarMiembro(1L, newMember);

        // Assert
        verify(clubRepository, times(1)).findById(1L);
        verify(clubRepository, times(1)).save(testClub);
    }

    @Test
    void testAgregarMiembro_NonExistingClub_ThrowsException() {
        // Arrange
        when(clubRepository.findById(999L)).thenReturn(Optional.empty());

        User newMember = new User();
        newMember.setId(2L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            clubService.agregarMiembro(999L, newMember);
        });
        verify(clubRepository, times(1)).findById(999L);
        verify(clubRepository, never()).save(any(Club.class));
    }

    @Test
    void testEliminarMiembro_Administrator_ThrowsException() {
        // Arrange
        when(clubRepository.findById(1L)).thenReturn(Optional.of(testClub));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            clubService.eliminarMiembro(1L, testUser);
        });
        verify(clubRepository, times(1)).findById(1L);
        verify(clubRepository, never()).save(any(Club.class));
    }

    @Test
    void testDesactivarClub_ExistingClub_DesactivatesClub() {
        // Arrange
        when(clubRepository.findById(1L)).thenReturn(Optional.of(testClub));
        when(clubRepository.save(any(Club.class))).thenReturn(testClub);

        // Act
        clubService.desactivarClub(1L);

        // Assert
        assertFalse(testClub.getActivo());
        verify(clubRepository, times(1)).findById(1L);
        verify(clubRepository, times(1)).save(testClub);
    }

    @Test
    void testFindAllClubesByUsuario_ReturnsUserClubs() {
        // Arrange
        List<Club> userClubs = Arrays.asList(testClub);
        when(clubRepository.findAllClubesByUsuario(testUser)).thenReturn(userClubs);

        // Act
        List<Club> result = clubService.findAllClubesByUsuario(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Club", result.get(0).getNombre());
        verify(clubRepository, times(1)).findAllClubesByUsuario(testUser);
    }

    @Test
    void testUsuarioTienePermisosGestion_Administrator_ReturnsTrue() {
        // Act
        boolean result = clubService.usuarioTienePermisosGestion(testUser, testClub);

        // Assert
        assertTrue(result);
    }

    @Test
    void testGetClubStats_ReturnsCorrectStats() {
        // Arrange
        when(clubRepository.findById(1L)).thenReturn(Optional.of(testClub));
        when(clubRepository.countMiembrosByClub(testClub)).thenReturn(10L);
        when(clubRepository.countTorneosActivosByClub(testClub)).thenReturn(5L);

        // Act
        ClubService.ClubStats stats = clubService.getClubStats(1L);

        // Assert
        assertNotNull(stats);
        assertEquals(10, stats.getTotalMiembros());
        assertEquals(5, stats.getTorneosActivos());
        verify(clubRepository, times(1)).findById(1L);
        verify(clubRepository, times(1)).countMiembrosByClub(testClub);
        verify(clubRepository, times(1)).countTorneosActivosByClub(testClub);
    }
}
