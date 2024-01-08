package bg.sofia.uni.fmi.mjt.intelligenthome.center;

import bg.sofia.uni.fmi.mjt.intelligenthome.center.exceptions.DeviceAlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.intelligenthome.center.exceptions.DeviceNotFoundException;
import bg.sofia.uni.fmi.mjt.intelligenthome.device.AmazonAlexa;
import bg.sofia.uni.fmi.mjt.intelligenthome.device.DeviceType;
import bg.sofia.uni.fmi.mjt.intelligenthome.storage.MapDeviceStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IntelligentHomeCenterTest {

    @Mock
    MapDeviceStorage storage;

    @InjectMocks
    IntelligentHomeCenter center;

    @Test
    void testGetFirstNDevicesByRegistration_More(){
        MapDeviceStorage storage = new MapDeviceStorage();
        storage.store("meow", new AmazonAlexa("lexi",31, LocalDateTime.now()));
        storage.store("meow2", new AmazonAlexa("lexa",1, LocalDateTime.now()));

        IntelligentHomeCenter center = new IntelligentHomeCenter(storage);

        assertEquals(2, center.getFirstNDevicesByRegistration(3).size(), "Expected 2");
    }
    @Test
    void testGetFirstNDevicesByRegistration_InvlInput(){
        assertThrows(IllegalArgumentException.class, () -> center.getFirstNDevicesByRegistration(-1));
    }

    @Test
    void testGetTopNDevicesByPwrCnsmptn_InvlInput() {
        assertThrows(IllegalArgumentException.class, () -> center.getTopNDevicesByPowerConsumption(-1));
    }

    @Test
    void testGetTopNDevicesByPwrCnsmptn_More() {
        MapDeviceStorage storage = new MapDeviceStorage();
        storage.store("meow", new AmazonAlexa("lexi",31, LocalDateTime.now()));
        storage.store("meow2", new AmazonAlexa("lexa",1, LocalDateTime.now()));

        IntelligentHomeCenter center = new IntelligentHomeCenter(storage);

        assertEquals(2, center.getTopNDevicesByPowerConsumption(3).size(), "Expected 2");
    }

    @Test
    void testGetDeviceQuantityByType(){
        MapDeviceStorage storage = new MapDeviceStorage();
        storage.store("meow", new AmazonAlexa("lexi",31, LocalDateTime.now()));
        storage.store("meow2", new AmazonAlexa("lexa",1, LocalDateTime.now()));

        IntelligentHomeCenter center = new IntelligentHomeCenter(storage);

        assertEquals(2, center.getDeviceQuantityPerType(DeviceType.SMART_SPEAKER), "Expected 2");

    }

    @Test
    void testGetDeviceById(){
        AmazonAlexa alexa  = new AmazonAlexa("lexi",31, LocalDateTime.now());

        when(storage.exists(alexa.getId())).thenReturn(true);
        when(storage.get(alexa.getId())).thenReturn(alexa);
        try {
            assertEquals(alexa.getPowerConsumptionKWh(),center.getDeviceById(alexa.getId()).getPowerConsumptionKWh());
        } catch (DeviceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetDeviceById_UnregDevice(){
        AmazonAlexa alexa  = new AmazonAlexa("lexi",31, LocalDateTime.now());

        when(storage.exists(alexa.getId())).thenReturn(false);
        assertThrows(DeviceNotFoundException.class,()->center.getDeviceById(alexa.getId()));
    }

    @Test
    void testGetDeviceById_InvInput(){
        assertThrows(IllegalArgumentException.class, () -> center.getDeviceById(""));
    }

    @Test
    void testRegister(){
        AmazonAlexa alexa  = new AmazonAlexa("lexi",31, LocalDateTime.now());
        when(storage.exists(alexa.getId())).thenReturn(false);

        try {
            center.register(alexa);
        } catch (DeviceAlreadyRegisteredException e) {
            throw new RuntimeException(e);
        }
        verify(storage,times(1)).store(alexa.getId(), alexa);
    }

    @Test
    void testRegister_InvInput(){
        assertThrows(IllegalArgumentException.class,() -> center.register(null));
    }

    @Test
    void testRegister_RegisteredDev(){
        AmazonAlexa alexa  = new AmazonAlexa("lexi",31, LocalDateTime.now());
        when(storage.exists(alexa.getId())).thenReturn(true);
        assertThrows(DeviceAlreadyRegisteredException.class,() -> center.register(alexa));    }

    @Test
    void testUnregister_ValidDevice() {
        AmazonAlexa alexa  = new AmazonAlexa("lexi",31, LocalDateTime.now());
        when(storage.exists(alexa.getId())).thenReturn(true);
        when(storage.delete(alexa.getId())).thenReturn(true);

        try {
            center.unregister(alexa);
        } catch (DeviceNotFoundException e) {
            throw new RuntimeException(e);
        }

        verify(storage, times(1)).delete(alexa.getId());
    }

    @Test
    void testUnregister_InvalidDevice() {
        assertThrows(IllegalArgumentException.class,() -> center.unregister(null));
    }

    @Test
    void testUnregister_UnregisteredDevice() {
        AmazonAlexa alexa  = new AmazonAlexa("lexi",31, LocalDateTime.now());
        when(storage.exists(alexa.getId())).thenReturn(false);
        assertThrows(DeviceNotFoundException.class,() -> center.unregister(alexa));
    }
}
