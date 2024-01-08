package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.algorithm.Rijndael;
import bg.sofia.uni.fmi.mjt.space.exception.TimeFrameMismatchException;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MJTSpaceScannerTest {

    private static MJTSpaceScanner spaceScanner;
    private static SecretKey secretKey;
    private static final String ENCRYPTION_ALGORITHM = "AES"; // //  Advanced Encryption Standard
    private static final int KEY_SIZE_IN_BITS = 128;
    @BeforeAll
    static void setUp() throws NoSuchAlgorithmException {
        String missionsInput = "0,Company Name,Location,Datum,Detail,Status Rocket,\" Rocket\",Status Mission\n" +
                "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,\"50.0 \",Success\n" +
                "1,CASC,\"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China\",\"Thu Aug 06, 2020\",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,\"29.75 \",Success\n" +
                "2,SpaceX,\"Pad A, Boca Chica, Texas, USA\",\"Tue Aug 04, 2020\",Starship Prototype | 150 Meter Hop,StatusActive,,Success\n" +
                "3,Roscosmos,\"Site 200/39, Baikonur Cosmodrome, Kazakhstan\",\"Thu Jul 30, 2020\",Proton-M/Briz-M | Ekspress-80 & Ekspress-103,StatusActive,\"65.0 \",Success";
        String rocketsInput = "\"\",Name,Wiki,Rocket Height\n" +
                "169,Falcon 9 Block 5,https://en.wikipedia.org/wiki/Falcon_9,70.0 m\n" +
                "213,Long March 2D,https://en.wikipedia.org/wiki/Long_March_2D,41.06 m\n" +
                "371,Starship Prototype,https://en.wikipedia.org/wiki/SpaceX_Starship,50.0 m\n" +
                "294,Proton-M/Briz-M,https://en.wikipedia.org/wiki/Proton-M,58.2 m\n";
        // Generate a test SecretKey
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
        keyGenerator.init(KEY_SIZE_IN_BITS);
        secretKey = keyGenerator.generateKey();
        spaceScanner = new MJTSpaceScanner(
                new StringReader(missionsInput),
                new StringReader(rocketsInput),
                secretKey);
    }

    @Test
    void testConstructorIOException() {
        Reader missionsReader = new StringReader("invalid data\ninvalid data");
        Reader rocketsReader = new StringReader("invalid data\ninvalid data");

        assertThrows(RuntimeException.class, () -> {
            new MJTSpaceScanner(missionsReader, rocketsReader, null);
        }, "invalid data");
    }

    @Test
    void testGetAllMissionsByNullStatus() {
        // Test that IllegalArgumentException is thrown when missionStatus is null
        assertThrows(IllegalArgumentException.class, () -> spaceScanner.getAllMissions(null));
    }

    @Test
    void testGetTopNTallestRocketsWithInvalidN() {
        // Test that IllegalArgumentException is thrown when n is less than or equal to 0
        assertThrows(IllegalArgumentException.class, () -> spaceScanner.getTopNTallestRockets(0));
    }

    @Test
    void testGetAllRockets() {
        Collection<Rocket> allRockets = spaceScanner.getAllRockets();
        assertEquals(4, allRockets.size());
        // Add specific assertions based on your dataset
        assertTrue(allRockets.stream().anyMatch(rocket -> rocket.name().equals("Falcon 9 Block 5")));
        assertTrue(allRockets.stream().anyMatch(rocket -> rocket.name().equals("Long March 2D")));
        assertTrue(allRockets.stream().anyMatch(rocket -> rocket.name().equals("Starship Prototype")));
        assertTrue(allRockets.stream().anyMatch(rocket -> rocket.name().equals("Proton-M/Briz-M")));
    }

    @Test
    void testGetAllRocketsUnmodifiable() {
        Collection<Rocket> allRockets = spaceScanner.getAllRockets();

        assertThrows(UnsupportedOperationException.class, () -> allRockets.add(new Rocket("New Rocket", "", null, null)));
        assertThrows(UnsupportedOperationException.class, () -> allRockets.removeIf(rocket -> rocket.name().equals("Falcon 9 Block 5")));

        // You can add more assertions to test other modification operations if needed
    }

    @Test
    void testGetAllMissions() {
        Collection<Mission> allMissions = spaceScanner.getAllMissions();
        assertEquals(4, allMissions.size());
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyInvalidTimeFrame() {
        assertThrows(TimeFrameMismatchException.class,
                () -> spaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(
                        LocalDate.parse("2020-12-31"), LocalDate.parse("2020-01-01")));
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyNullTimeFrame() {
        assertAll(() -> {
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(null, LocalDate.parse("2020-12-31")));
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(LocalDate.parse("2020-01-01"), null));
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(null, null));
        });
    }

    @Test
    void testGetAllMissionsByStatus() {
        Collection<Mission> missionsByStatus = spaceScanner.getAllMissions(MissionStatus.SUCCESS);
        assertEquals(4, missionsByStatus.size());
        assertEquals(MissionStatus.SUCCESS, missionsByStatus.iterator().next().missionStatus());
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionsInvalidArguments() {
        assertAll(() -> {
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.getCompanyWithMostSuccessfulMissions(null, LocalDate.parse("2020-12-31")));
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.getCompanyWithMostSuccessfulMissions(LocalDate.parse("2020-01-01"), null));
            assertThrows(TimeFrameMismatchException.class,
                    () -> spaceScanner.getCompanyWithMostSuccessfulMissions(
                            LocalDate.parse("2020-12-31"), LocalDate.parse("2020-01-01")));
        });
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissions() {
        String company = spaceScanner.getCompanyWithMostSuccessfulMissions(
                LocalDate.parse("2020-01-01"), LocalDate.parse("2020-12-31"));
        assertEquals("SpaceX", company);
    }

    @Test
    void testGetMissionsPerCountry() {
        Map<String, Collection<Mission>> missionsPerCountry = spaceScanner.getMissionsPerCountry();
        assertEquals(3, missionsPerCountry.size());
    }

    @Test
    void testGetTopNLeastExpensiveMissionsInvalidArguments() {
        assertAll(() -> {
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.getTopNLeastExpensiveMissions(-1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE));
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.getTopNLeastExpensiveMissions(0, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE));
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.getTopNLeastExpensiveMissions(1, null, RocketStatus.STATUS_ACTIVE));
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.getTopNLeastExpensiveMissions(1, MissionStatus.SUCCESS, null));
        });
    }


    @Test
    void testGetTopNLeastExpensiveMissions() {
        List<Mission> topMissions = spaceScanner.getTopNLeastExpensiveMissions(2, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE);
        assertEquals(2, topMissions.size());
        assertTrue(topMissions.stream().allMatch(mission -> mission.cost().isPresent()));
        assertTrue(topMissions.get(0).cost().get() < topMissions.get(1).cost().get());
    }

    @Test
    void testGetMostDesiredLocationForMissionsPerCompany() {
        Map<String, String> desiredLocations = spaceScanner.getMostDesiredLocationForMissionsPerCompany();
        assertEquals(3, desiredLocations.size());
        assertTrue(desiredLocations.containsKey("SpaceX"));
        assertTrue(desiredLocations.containsKey("CASC"));
        assertTrue(desiredLocations.containsKey("Roscosmos"));
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompany() {
        Map<String, String> locations = spaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(
                LocalDate.parse("2020-01-01"), LocalDate.parse("2020-12-31"));
        assertEquals(3, locations.size());
        assertTrue(locations.containsKey("SpaceX"));
        assertTrue(locations.containsKey("CASC"));
        assertTrue(locations.containsKey("Roscosmos"));
    }

    @Test
    void testGetTopNTallestRockets() {
        List<Rocket> topRockets = spaceScanner.getTopNTallestRockets(1);
        assertEquals(1, topRockets.size());
        assertTrue(topRockets.get(0).height().isPresent());
        assertEquals(70.0, topRockets.get(0).height().get());
    }


    @Test
    void testGetWikiPageForRocket() {
        Map<String, Optional<String>> wikiPages = spaceScanner.getWikiPageForRocket();
        assertEquals(4, wikiPages.size());
        assertTrue(wikiPages.get("Falcon 9 Block 5").isPresent());
        assertTrue(wikiPages.get("Long March 2D").isPresent());
        assertTrue(wikiPages.get("Starship Prototype").isPresent());
        assertTrue(wikiPages.get("Proton-M/Briz-M").isPresent());
    }
    // Add more tests for the remaining methods...

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsInvalidArguments() {
        assertAll(() -> {
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(-1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE));
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(0, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE));
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, null, RocketStatus.STATUS_ACTIVE));
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, MissionStatus.SUCCESS, null));
        });
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissions() {
        List<String> wikiPages = spaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(
                2, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE);

        assertEquals(2, wikiPages.size());
        // Assuming the dataset in the setup, these are the rockets used in the two most expensive missions
        assertTrue(wikiPages.contains("https://en.wikipedia.org/wiki/Falcon_9"));
        assertTrue(wikiPages.contains("https://en.wikipedia.org/wiki/Proton-M"));
    }

    @Test
    void testSaveMostReliableRocketInvalidArguments() {
        assertAll(() -> {
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.saveMostReliableRocket(null, LocalDate.parse("2020-01-01"), LocalDate.parse("2020-12-31")));
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.saveMostReliableRocket(new ByteArrayOutputStream(), null, LocalDate.parse("2020-12-31")));
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.saveMostReliableRocket(new ByteArrayOutputStream(), LocalDate.parse("2020-01-01"), null));
            assertThrows(IllegalArgumentException.class,
                    () -> spaceScanner.saveMostReliableRocket(null, null, null));
            assertThrows(TimeFrameMismatchException.class,
                    () -> spaceScanner.saveMostReliableRocket(new ByteArrayOutputStream(),
                            LocalDate.parse("2020-12-31"), LocalDate.parse("2020-01-01")));
        });
    }

    @Test
    void testSaveMostReliableRocket() {
        try {
            // Create a ByteArrayOutputStream to capture the output
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Invoke the method
            spaceScanner.saveMostReliableRocket(outputStream, LocalDate.parse("2020-01-01"), LocalDate.parse("2020-12-31"));

            // Decode the encrypted content using the test SecretKey
            //String encryptedContent = outputStream.toString(StandardCharsets.UTF_8);
            //byte[] decodedBytes = new byte[10000];

            // Decrypt the content using the actual Rijndael class in MJTSpaceScanner
            ByteArrayInputStream encryptedInput = new ByteArrayInputStream(outputStream.toByteArray());
            ByteArrayOutputStream decryptedOutput = new ByteArrayOutputStream();
            Rijndael rijndael = new Rijndael(secretKey);
            rijndael.decrypt(encryptedInput, decryptedOutput);

            // Assert the decrypted content (this will depend on your specific implementation)
            String decryptedContent = decryptedOutput.toString(StandardCharsets.UTF_8);
            System.out.println(decryptedContent);
            // Add your specific assertions for the decrypted content here

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

}