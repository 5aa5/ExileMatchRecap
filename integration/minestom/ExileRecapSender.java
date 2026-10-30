package net.playexile.recap.integration.minestom;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.PluginMessagePacket;
import net.playexile.recap.api.MatchRecap;
import net.playexile.recap.api.RecapCodec;

public final class ExileRecapSender {
  private ExileRecapSender() {
  }

  public static void send(Player player, MatchRecap recap) {
    player.sendPacket(new PluginMessagePacket(RecapCodec.CHANNEL, RecapCodec.encode(recap)));
  }
}
