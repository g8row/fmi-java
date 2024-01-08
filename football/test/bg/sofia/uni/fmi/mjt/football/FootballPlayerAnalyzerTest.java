package bg.sofia.uni.fmi.mjt.football;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FootballPlayerAnalyzerTest {

    private static FootballPlayerAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        String sampleData = "name;full_name;birth_date;age;height_cm;weight_kgs;positions;nationality;overall_rating;potential;value_euro;wage_euro;preferred_foot" + System.lineSeparator() +
                "player1;Full Name 1;01/01/1995;28;180.0;75.0;ST;Nationality1;80;85;1000;500;Right" + System.lineSeparator() +
                "player2;Full Name 2;01/01/1993;30;175.0;70.0;LM,CM;Nationality1;75;80;1200;550;Left" + System.lineSeparator() +
                "player3;Full Name 3;01/01/2000;22;185.0;80.0;ST,CF;Nationality2;80;95;1500;700;Right" + System.lineSeparator();
        analyzer = new FootballPlayerAnalyzer(new StringReader(sampleData));
    }

    @Test
    void testGetAllPlayers() {
        List<Player> allPlayers = analyzer.getAllPlayers();
        assertEquals(3, allPlayers.size());
    }

    @Test
    void testGetAllNationalities() {
        Set<String> allNationalities = analyzer.getAllNationalities();
        assertEquals(Set.of("Nationality1", "Nationality2"), allNationalities);
    }

    @Test
    void testGetHighestPaidPlayerByNationality() {
        Player highestPaid = analyzer.getHighestPaidPlayerByNationality("Nationality1");
        assertEquals("player2", highestPaid.name());
    }

    @Test
    void testGetHighestPaidPlayerByNationalityWithNoSuchElementException() {
        assertThrows(NoSuchElementException.class,
                () -> analyzer.getHighestPaidPlayerByNationality("NonExistentNationality"));
    }

    @Test
    void testGroupByPosition() {
        var groupedByPosition = analyzer.groupByPosition();
        assertTrue(groupedByPosition.containsKey(Position.ST));
        assertTrue(groupedByPosition.containsKey(Position.LM));
    }

    @Test
    void testGetTopProspectPlayerForPositionInBudget() {
        Optional<Player> topProspect = analyzer.getTopProspectPlayerForPositionInBudget(Position.ST, 1300);
        assertTrue(topProspect.isPresent());
        assertEquals("player1", topProspect.get().name());
    }

    @Test
    void testGetTopProspectPlayerForPositionInBudgetWithIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                analyzer.getTopProspectPlayerForPositionInBudget(null, 1300));
    }

    @Test
    void testGetSimilarPlayers() {
        Player playerToCompare = new Player("player1", "Full Name 1", LocalDate.parse("1995-01-01"),
                28, 180.0, 75.0, Arrays.asList(Position.ST), "Nationality1",
                80, 85, 1000, 500, Foot.RIGHT);

        Set<Player> similarPlayers = analyzer.getSimilarPlayers(playerToCompare);

        assertEquals(2, similarPlayers.size());
    }

    @Test
    void testGetPlayersByFullNameKeyword() {
        Set<Player> playersWithKeyword = analyzer.getPlayersByFullNameKeyword("Name");
        assertEquals(3, playersWithKeyword.size());
    }

    @Test
    void testGetPlayersByFullNameKeywordWithNullKeyword() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.getPlayersByFullNameKeyword(null));
    }

    @Test
    void testGetHighestPaidPlayerByNationalityNull() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.getHighestPaidPlayerByNationality(null));
    }

    @Test
    void testGetSimilarPlayersNull() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.getSimilarPlayers(null));
    }
}



