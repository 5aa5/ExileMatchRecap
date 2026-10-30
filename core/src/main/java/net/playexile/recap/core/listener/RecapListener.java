package net.playexile.recap.core.listener;

import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.server.NetworkPayloadEvent;
import net.labymod.api.event.client.network.server.NetworkPayloadEvent.Side;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.playexile.recap.api.RecapCodec;
import net.playexile.recap.core.RecapAddon;

public final class RecapListener {
  private final RecapAddon addon;

  public RecapListener(RecapAddon addon) {
    this.addon = addon;
  }

  @Subscribe
  public void onPayload(NetworkPayloadEvent event) {
    if (event.side() != Side.RECEIVE
        || !event.identifier().getNamespace().equals("exile")
        || !event.identifier().getPath().equals("recap")
        || !this.addon.configuration().enabled().get()) {
      return;
    }

    try {
      this.addon.state().show(
          RecapCodec.decode(event.getPayload()),
          this.addon.configuration().displaySeconds().get()
      );
    } catch (IllegalArgumentException ignored) {
      // Ignore packets from incompatible or misconfigured servers.
    }
  }

  @Subscribe
  public void onDisconnect(ServerDisconnectEvent event) {
    this.addon.state().dismiss();
  }
}
