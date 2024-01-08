package bg.sofia.uni.fmi.mjt.intelligenthome.device;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RgbBulbTest {
    @Test
    void getIdTest(){
        RgbBulb alexa1 = new RgbBulb("bruh", 1, LocalDateTime.now());
        RgbBulb alexa2 = new RgbBulb("bruh", 1, LocalDateTime.now());
        RgbBulb alexa3 = new RgbBulb("bruh", 1, LocalDateTime.now());

        assertEquals(alexa3.getId(), DeviceType.BULB.getShortName() + "-bruh-2");
    }
}
