package net.playexile.recap.api;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecapCodecTest {
  @Test
  void roundTrip() {
    Map<String, Integer> stats = new LinkedHashMap<>();
    stats.put("kills", 7);
    stats.put("final_kills", 3);
    stats.put("beds_broken", 1);
    MatchRecap sent = new MatchRecap(
        UUID.randomUUID(), "bedwars", "doubles", MatchRecap.Result.VICTORY,
        762, 35, stats
    );

    assertEquals(sent, RecapCodec.decode(RecapCodec.encode(sent)));
  }

  @Test
  void rejectsUnknownVersion() {
    byte[] payload = RecapCodec.encode(new MatchRecap(
        UUID.randomUUID(), "timv", "classic", MatchRecap.Result.DEFEAT,
        100, 0, Map.of("kills", 1)
    ));
    payload[0] = 2;
    assertThrows(IllegalArgumentException.class, () -> RecapCodec.decode(payload));
  }

  @Test
  void rejectsMalformedPayloads() {
    assertThrows(IllegalArgumentException.class, () -> RecapCodec.decode(new byte[0]));
    assertThrows(IllegalArgumentException.class, () -> RecapCodec.decode(new byte[2049]));
    assertThrows(IllegalArgumentException.class, () -> RecapCodec.decode(new byte[] {1, 0}));
  }

  @Test
  void refusesExtraStatistics() {
    assertThrows(IllegalArgumentException.class, () -> new MatchRecap(
        UUID.randomUUID(), "bedwars", "solo", MatchRecap.Result.DRAW, 0, 0,
        Map.of("a", 1, "b", 2, "c", 3, "d", 4, "e", 5)
    ));
  }
}
