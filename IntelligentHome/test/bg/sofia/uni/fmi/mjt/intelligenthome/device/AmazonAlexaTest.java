package bg.sofia.uni.fmi.mjt.intelligenthome.device;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class AmazonAlexaTest {
    @Test
    void getIdTest(){
        AmazonAlexa alexa1 = new AmazonAlexa("bruh", 1, LocalDateTime.now());
        AmazonAlexa alexa2 = new AmazonAlexa("bruh", 1, LocalDateTime.now());
        AmazonAlexa alexa3 = new AmazonAlexa("bruh", 1, LocalDateTime.now());

        assertEquals(alexa3.getId(), DeviceType.SMART_SPEAKER.getShortName() + "-bruh-2");
    }
}
