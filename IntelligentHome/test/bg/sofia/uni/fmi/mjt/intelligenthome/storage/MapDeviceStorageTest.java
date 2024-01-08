package bg.sofia.uni.fmi.mjt.intelligenthome.storage;

import bg.sofia.uni.fmi.mjt.intelligenthome.device.IoTDevice;
import bg.sofia.uni.fmi.mjt.intelligenthome.device.AmazonAlexa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class MapDeviceStorageTest {
    @Mock
    Map<String, IoTDevice> devices;

    @InjectMocks
    MapDeviceStorage storage;

    @Test
    void testDeleteInvInput(){
        assertThrows(IllegalArgumentException.class, () -> storage.delete(null));
    }

    @Test
    void testDeleteInvalidDevice(){
        AmazonAlexa alexa  = new AmazonAlexa("lexi",31, LocalDateTime.now());
        assertFalse(storage.delete(alexa.getId()));
    }

    @Test
    void testDeleteValidDevice(){
        AmazonAlexa alexa  = new AmazonAlexa("lexi",31, LocalDateTime.now());
        MapDeviceStorage storage = new MapDeviceStorage();
        storage.store(alexa.getId(), alexa);
        assertTrue(storage.delete(alexa.getId()));
    }

}
