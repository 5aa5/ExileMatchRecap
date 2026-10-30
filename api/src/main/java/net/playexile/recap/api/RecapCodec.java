package net.playexile.recap.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class RecapCodec {
  public static final String CHANNEL = "exile:recap";
  public static final int VERSION = 1;
  public static final int MAX_PACKET_SIZE = 2048;

  private RecapCodec() {
  }

  public static byte[] encode(MatchRecap recap) {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream(256);
      DataOutputStream output = new DataOutputStream(buffer);
      output.writeByte(VERSION);
      output.writeLong(recap.matchId().getMostSignificantBits());
      output.writeLong(recap.matchId().getLeastSignificantBits());
      output.writeUTF(recap.game());
      output.writeUTF(recap.mode());
      output.writeByte(recap.result().ordinal());
      output.writeInt(recap.durationSeconds());
      output.writeInt(recap.goldEarned());
      output.writeByte(recap.stats().size());

      for (Map.Entry<String, Integer> stat : recap.stats().entrySet()) {
        output.writeUTF(stat.getKey());
        output.writeInt(stat.getValue());
      }

      output.flush();
      byte[] bytes = buffer.toByteArray();
      if (bytes.length > MAX_PACKET_SIZE) {
        throw new IllegalArgumentException("Recap packet exceeds size limit");
      }
      return bytes;
    } catch (IOException exception) {
      throw new IllegalStateException("Could not encode match recap", exception);
    }
  }

  public static MatchRecap decode(byte[] payload) {
    if (payload == null || payload.length == 0 || payload.length > MAX_PACKET_SIZE) {
      throw new IllegalArgumentException("Invalid recap packet size");
    }

    try {
      DataInputStream input = new DataInputStream(new ByteArrayInputStream(payload));
      if (input.readUnsignedByte() != VERSION) {
        throw new IllegalArgumentException("Unsupported recap protocol version");
      }

      UUID matchId = new UUID(input.readLong(), input.readLong());
      String game = input.readUTF();
      String mode = input.readUTF();
      int outcome = input.readUnsignedByte();
      if (outcome >= MatchRecap.Result.values().length) {
        throw new IllegalArgumentException("Invalid match result");
      }

      int durationSeconds = input.readInt();
      int goldEarned = input.readInt();
      int statCount = input.readUnsignedByte();
      if (statCount > 4) {
        throw new IllegalArgumentException("Too many statistics");
      }

      Map<String, Integer> stats = new LinkedHashMap<>();
      for (int index = 0; index < statCount; index++) {
        String key = input.readUTF();
        int value = input.readInt();
        if (stats.putIfAbsent(key, value) != null) {
          throw new IllegalArgumentException("Duplicate statistic");
        }
      }
      if (input.available() != 0) {
        throw new IllegalArgumentException("Unexpected trailing bytes");
      }

      return new MatchRecap(
          matchId, game, mode, MatchRecap.Result.values()[outcome],
          durationSeconds, goldEarned, stats
      );
    } catch (IOException exception) {
      throw new IllegalArgumentException("Malformed recap packet", exception);
    }
  }
}
