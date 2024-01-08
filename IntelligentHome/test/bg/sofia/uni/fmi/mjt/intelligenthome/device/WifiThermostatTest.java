package bg.sofia.uni.fmi.mjt.intelligenthome.device;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class WifiThermostatTest {
    @Test
    void getIdTest(){
        WiFiThermostat alexa1 = new WiFiThermostat("bruh", 1, LocalDateTime.now());
        WiFiThermostat alexa2 = new WiFiThermostat("bruh", 1, LocalDateTime.now());
        WiFiThermostat alexa3 = new WiFiThermostat("bruh", 1, LocalDateTime.now());

        assertEquals(alexa3.getId(), DeviceType.THERMOSTAT.getShortName() + "-bruh-2");
    }
}
