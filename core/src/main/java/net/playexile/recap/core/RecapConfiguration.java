package net.playexile.recap.core;

import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.property.ConfigProperty;

@ConfigName("settings")
public final class RecapConfiguration extends AddonConfig {
  @SwitchSetting
  private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

  @SliderSetting(min = 4, max = 20)
  private final ConfigProperty<Integer> displaySeconds = new ConfigProperty<>(8);

  @SwitchSetting
  private final ConfigProperty<Boolean> subtleMotion = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> floatingBlocks = new ConfigProperty<>(true);

  @Override
  public ConfigProperty<Boolean> enabled() {
    return this.enabled;
  }

  public ConfigProperty<Integer> displaySeconds() {
    return this.displaySeconds;
  }

  public ConfigProperty<Boolean> subtleMotion() {
    return this.subtleMotion;
  }

  public ConfigProperty<Boolean> floatingBlocks() {
    return this.floatingBlocks;
  }
}
