package net.playexile.recap.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record MatchRecap(
    UUID matchId,
    String game,
    String mode,
    Result result,
    int durationSeconds,
    int goldEarned,
    Map<String, Integer> stats
) {
  public MatchRecap {
    Objects.requireNonNull(matchId, "matchId");
    Objects.requireNonNull(result, "result");
    checkKey(game, "game");
    checkKey(mode, "mode");

    if (durationSeconds < 0 || durationSeconds > 86400) {
      throw new IllegalArgumentException("Invalid match duration");
    }
    if (goldEarned < 0 || goldEarned > 10000000) {
      throw new IllegalArgumentException("Invalid gold reward");
    }
    Objects.requireNonNull(stats, "stats");
    if (stats.size() > 4) {
      throw new IllegalArgumentException("Too many match statistics");
    }

    Map<String, Integer> copy = new LinkedHashMap<>();
    for (Map.Entry<String, Integer> entry : stats.entrySet()) {
      checkKey(entry.getKey(), "statistic");
      Integer value = Objects.requireNonNull(entry.getValue(), "stat value");
      if (value < 0 || value > 10000000) {
        throw new IllegalArgumentException("Invalid statistic value");
      }
      copy.put(entry.getKey(), value);
    }
    stats = Collections.unmodifiableMap(copy);
  }

  private static void checkKey(String key, String label) {
    if (key == null || !key.matches("[a-z0-9_]{1,32}")) {
      throw new IllegalArgumentException("Invalid " + label + " identifier");
    }
  }

  public enum Result {
    VICTORY,
    DEFEAT,
    DRAW
  }
}
