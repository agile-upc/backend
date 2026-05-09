package com.agrotech.api.management.application.internal.commandservices;
import com.agrotech.api.management.domain.model.commands.CreateEnclosureCommand;
import com.agrotech.api.management.infrastructure.persistence.jpa.repositories.EnclosureRepository;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repositories.FarmerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;
import static org.mockito.Mockito.*;
import com.agrotech.api.management.infrastructure.persistence.jpa.entities.EnclosureEntity;
import com.agrotech.api.profile.infrastructure.persistence.jpa.entities.FarmerEntity;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class EnclosureCommandServiceImplTest {

    @Mock
    private EnclosureRepository enclosureRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @InjectMocks
    private EnclosureCommandServiceImpl enclosureCommandService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Creating an enclosure with valid data returns the enclosure ID
    @Test
    public void test_handle_create_enclosure_command_returns_enclosure_id() {
        // Arrange
        Long farmerId = 1L;
        Long expectedEnclosureId = 100L;
        String name = "Cattle Pen";
        Integer capacity = 50;
        String type = "Cattle";

        CreateEnclosureCommand command = new CreateEnclosureCommand(name, capacity, type, farmerId);

        // Mock farmer entity
        FarmerEntity farmerEntity = mock(FarmerEntity.class);
        when(farmerRepository.findById(farmerId)).thenReturn(Optional.of(farmerEntity));

        // Mock saved enclosure entity with an ID
        EnclosureEntity savedEnclosure = mock(EnclosureEntity.class);
        when(savedEnclosure.getId()).thenReturn(expectedEnclosureId);

        // When saving, return the saved enclosure
        when(enclosureRepository.save(any(EnclosureEntity.class))).thenReturn(savedEnclosure);

        // Act
        Long result = enclosureCommandService.handle(command);

        // Assert
        assertEquals(expectedEnclosureId, result);
        verify(farmerRepository).findById(farmerId);
        verify(enclosureRepository).save(any(EnclosureEntity.class));
    }
}
