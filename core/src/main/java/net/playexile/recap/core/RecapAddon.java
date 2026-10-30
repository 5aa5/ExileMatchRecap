package net.playexile.recap.core;

import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.models.addon.annotation.AddonMain;
import net.playexile.recap.core.hud.RecapHudWidget;
import net.playexile.recap.core.listener.RecapListener;

@AddonMain
public final class RecapAddon extends LabyAddon<RecapConfiguration> {
  private final RecapState state = new RecapState();
  private HudWidgetCategory widgetCategory;

  @Override
  protected void enable() {
    this.registerSettingCategory();
    this.widgetCategory = new HudWidgetCategory("exilerecap");
    this.labyAPI().hudWidgetRegistry().categoryRegistry().register(this.widgetCategory);
    this.labyAPI().hudWidgetRegistry().register(new RecapHudWidget(this));
    this.registerListener(new RecapListener(this));
  }

  @Override
  protected Class<RecapConfiguration> configurationClass() {
    return RecapConfiguration.class;
  }

  public RecapState state() {
    return this.state;
  }

  public HudWidgetCategory widgetCategory() {
    return this.widgetCategory;
  }
}
