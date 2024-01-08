package bg.sofia.uni.fmi.mjt.football;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FootballPlayerAnalyzer {
    List<Player> players;
    /**
     * Loads the dataset from the given {@code reader}. The reader argument will not be null and a correct dataset of
     * the specified type can be read from it.
     *
     * @param reader Reader from which the dataset can be read.
     */
    public FootballPlayerAnalyzer(Reader reader) {
        StringBuilder csv = new StringBuilder();
        int charRead;
        while (true) {
            try {
                if ((charRead = reader.read()) < 0) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            csv.append((char) charRead);
        }
        String[] rows = csv.toString().split(System.lineSeparator());
        rows = Arrays.copyOfRange(rows, 1, rows.length);
        players = Arrays.stream(rows).map((x) -> {
            if ((x.split("\\Q" + ';' + "\\E")).length != Player.NUM_OF_ATTRIBUTES) {
                throw new IllegalArgumentException("wrong dataset");
            }
            return Player.of(x);
        }).toList();
    }

    /**
     * Returns all players from the dataset in undefined order as an unmodifiable List. If the dataset is empty, returns
     * an empty List.
     *
     * @return the list of all players.
     */
    public List<Player> getAllPlayers() {
        return List.copyOf(players);
    }

    /**
     * Returns an unmodifiable set of all nationalities in the dataset. If the dataset is empty, returns an empty Set.
     *
     * @return the set of all nationalities
     */
    public Set<String> getAllNationalities() {
        return players.stream().map(Player::nationality).collect(Collectors.toSet());
    }

    /**
     * Returns the highest paid player from the provided nationality. If there are two or more players with equal
     * maximum wage, returns any of them.
     *
     * @param nationality the nationality of the player to return
     * @return the highest paid player
     * @throws IllegalArgumentException in case the provided nationality is null
     * @throws NoSuchElementException   in case there is no player with the provided nationality
     */
    public Player getHighestPaidPlayerByNationality(String nationality) {
        if (nationality == null || nationality.isBlank()) {
            throw new IllegalArgumentException();
        }
        Player result =
                players.stream()
                        .filter(x -> x.nationality().equals(nationality))
                        .reduce(null, (res, el) -> {
                            if (res == null || el.wageEuro() > res.wageEuro()) {
                                return el;
                            }
                            return res;
                        });
        if (result == null) {
            throw new NoSuchElementException();
        }
        return result;
    }

    /**
     * Returns a breakdown of players by position. Note that some players can play in more than one position so they
     * should be present in more than one value Set. If no player plays in a given Position then that position should
     * not be present as a key in the map.
     *
     * @return a Map with key: a Position and value: the set of players in the dataset that can play in that Position,
     * in undefined order.
     */
    public Map<Position, Set<Player>> groupByPosition() {
        Map<Position, Set<Player>> result = new HashMap<>();
        players.forEach(x -> x.positions().forEach((pos) -> {
            result.computeIfAbsent(pos, k -> new HashSet<>());
            result.get(pos).add(x);
        }));
        return result;
    }

    /**
     * Returns an Optional containing the top prospect player in the dataset that can play in the provided position and
     * that can be bought with the provided budget considering the player's value_euro. If no player can be bought with
     * the provided budget then return an empty Optional.
     * <p>
     * The player's prospect is calculated by the following formula: Prospect = (r + p) ÷ a where r is the player's
     * overall rating, p is the player's potential and a is the player's age
     *
     * @param position the position in which the player should be able to play
     * @param budget   the available budget for buying a player
     * @return an Optional containing the top prospect player
     * @throws IllegalArgumentException in case the provided position is null or the provided budget is negative
     */
    public Optional<Player> getTopProspectPlayerForPositionInBudget(Position position, long budget) {
        if (position == null || budget < 0) {
            throw new IllegalArgumentException();
        }
        return groupByPosition().get(position).stream()
                .filter(x -> x.valueEuro() < budget)
                .max(Comparator.comparingDouble(x -> (double) (x.overallRating() + x.potential()) / x.age()));
    }

    /**
     * Returns an unmodifiable set of players that are similar to the provided player. Two players are considered
     * similar if: 1. there is at least one position in which both of them can play 2. both players prefer the same foot
     * 3. their overall_rating measures differ by at most 3 (inclusive)
     * If the dataset contains the provided player, the player will be present in the returned result.
     *
     * @param player the player for whom similar players are retrieved. It may or may not be part of the dataset.
     * @return an unmodifiable set of similar players
     * @throws IllegalArgumentException if the provided player is null
     */
    public Set<Player> getSimilarPlayers(Player player) {
        if (player == null) {
            throw new IllegalArgumentException();
        }
        final int minDifInRating = 3;
        return Set.copyOf(players.stream()
                .filter(x -> !x.positions().stream()
                        .filter(player.positions()::contains)
                        .collect(Collectors.toSet()).isEmpty()
                        && x.preferredFoot() == player.preferredFoot()
                        && Math.abs(x.overallRating() - player.overallRating()) <= minDifInRating)
                .collect(Collectors.toSet()));
    }

    /**
     * Returns an unmodifiable set of players whose full name contains the provided keyword (case-sensitive search)
     *
     * @param keyword the keyword that should be contained in player's full name
     * @return an unmodifiable set of players
     * @throws IllegalArgumentException if the provided keyword is null
     */
    public Set<Player> getPlayersByFullNameKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException();
        }
        return Set.copyOf(players.stream()
                .filter(x -> x.fullName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toSet()));
    }
}