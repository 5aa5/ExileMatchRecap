package net.playexile.recap.core.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.component.format.TextDecoration;
import net.labymod.api.client.gfx.pipeline.renderer.text.TextRenderingOptions;
import net.labymod.api.client.gui.hud.hudwidget.HudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.SimpleHudWidget;
import net.labymod.api.client.gui.hud.position.HudSize;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.util.bounds.Rectangle;
import net.playexile.recap.api.MatchRecap;
import net.playexile.recap.core.RecapAddon;

public final class RecapHudWidget extends SimpleHudWidget<HudWidgetConfig> {
  private static final float FRAME_X = 9.0F;
  private static final float FRAME_Y = 7.0F;
  private static final float PADDING = 10.0F;
  private static final float MIN_CONTENT_WIDTH = 236.0F;
  private static final float GRID_Y = 95.0F;
  private static final float CELL_HEIGHT = 32.0F;
  private static final float CELL_GAP = 5.0F;
  private static final int CENTERED_SHADOW = TextRenderingOptions.CENTERED | TextRenderingOptions.SHADOW;

  private static final MatchRecap PREVIEW = new MatchRecap(
      new UUID(0L, 0L), "bedwars", "doubles", MatchRecap.Result.VICTORY, 762, 35,
      previewStats()
  );

  private final RecapAddon addon;
  private MatchRecap displayed;
  private Layout layout;

  public RecapHudWidget(RecapAddon addon) {
    super("match_recap", HudWidgetConfig.class);
    this.addon = addon;
    this.bindCategory(addon.widgetCategory());
  }

  @Override
  public boolean isVisibleInGame() {
    return this.addon.configuration().enabled().get() && this.addon.state().active() != null;
  }

  @Override
  public void render(RenderPhase phase, ScreenContext context, boolean editor, HudSize size) {
    MatchRecap recap = editor ? PREVIEW : this.addon.state().active();
    if (recap == null) {
      size.set(0.0F, 0.0F);
      return;
    }

    if (recap != this.displayed || this.layout == null) {
      this.displayed = recap;
      this.layout = prepare(recap);
    }

    Layout current = this.layout;
    size.set(current.width(), current.height());
    if (phase == null || !phase.canRender()) {
      return;
    }

    boolean motion = !editor && this.addon.configuration().subtleMotion().get();
    long now = System.nanoTime();
    float entering = motion ? ease(clamp(this.addon.state().elapsedNanos(now) / 260_000_000.0F)) : 1.0F;
    float leaving = motion ? clamp(this.addon.state().remainingNanos(now) / 200_000_000.0F) : 1.0F;
    float alpha = Math.max(0.0F, Math.min(entering, leaving));
    float offsetY = motion ? (1.0F - entering) * 5.0F - (1.0F - leaving) * 2.0F : 0.0F;

    context.pushStack();
    try {
      context.translate(0.0F, offsetY, 0.0F);
      drawCard(context, current, alpha);
      if (this.addon.configuration().floatingBlocks().get()) {
        drawBlocks(context, current, alpha, motion ? now / 1_000_000_000.0 : 0.0);
      }
    } finally {
      context.popStack();
    }
  }

  private void drawCard(ScreenContext context, Layout view, float alpha) {
    float frameWidth = view.width() - FRAME_X * 2.0F;
    float frameHeight = view.height() - FRAME_Y * 2.0F;
    float innerX = FRAME_X + PADDING;
    float innerWidth = frameWidth - PADDING * 2.0F;

    drawFills(context, view.frameFills(), alpha);

    float badgeWidth = view.result().getWidth() + 13.0F;
    float badgeX = FRAME_X + frameWidth - PADDING - badgeWidth;

    text(context, view.brand(), innerX, 16.0F);
    text(context, view.subtitle(), innerX + view.brand().getWidth() + 7.0F, 17.0F);
    text(context, view.result(), badgeX + badgeWidth / 2.0F, 16.0F, true);
    text(context, view.game(), innerX, 34.0F);
    text(context, view.mode(), innerX, 47.0F);

    if (view.goldValue() == null) {
      float center = innerX + innerWidth / 2.0F;
      text(context, view.durationLabel(), center, 65.0F, true);
      text(context, view.durationValue(), center, 76.0F, true);
    } else {
      text(context, view.durationLabel(), innerX, 65.0F);
      text(context, view.durationValue(), innerX, 76.0F);
      float secondColumn = innerX + innerWidth / 2.0F + 3.0F;
      text(context, view.goldLabel(), secondColumn, 65.0F);
      text(context, view.goldValue(), secondColumn, 76.0F);
    }

    if (view.cells().isEmpty()) {
      text(context, view.empty(), innerX + innerWidth / 2.0F, GRID_Y + 12.0F, true);
      return;
    }

    float cellWidth = (innerWidth - CELL_GAP) / 2.0F;
    for (int i = 0; i < view.cells().size(); i++) {
      Cell cell = view.cells().get(i);
      float x = innerX + (i % 2) * (cellWidth + CELL_GAP);
      float y = GRID_Y + (i / 2) * (CELL_HEIGHT + CELL_GAP);
      drawFills(context, view.cellFills().get(i), alpha);
      float center = x + cellWidth / 2.0F;
      text(context, cell.label(), center, y + 3.0F, true);
      text(context, cell.value(), center, y + 17.0F, true);
    }
  }

  private void drawBlocks(ScreenContext context, Layout view, float alpha, double time) {
    float farEdge = view.width() - 6.0F;
    drawBlock(context, view.blockFills().get(0), 3.0F, 37.0F + bob(time, 0.0), alpha * 0.62F);
    drawBlock(context, view.blockFills().get(1), farEdge, 32.0F + bob(time, 1.7), alpha * 0.53F);
    drawBlock(context, view.blockFills().get(2), farEdge - 1.0F,
        view.height() - 44.0F + bob(time, 3.1), alpha * 0.38F);
  }

  private static float bob(double time, double phase) {
    return (float) (Math.sin(time * 1.35 + phase) * 1.25);
  }

  private static void drawBlock(ScreenContext context, List<Fill> fills, float x, float y, float alpha) {
    context.pushStack();
    try {
      context.translate(x, y, 0.0F);
      drawFills(context, fills, alpha);
    } finally {
      context.popStack();
    }
  }

  private static void drawFills(ScreenContext context, List<Fill> fills, float alpha) {
    for (Fill fill : fills) {
      context.canvas().submitRect(fill.rectangle(), opacity(fill.color(), alpha));
    }
  }

  private static void fill(List<Fill> fills, float x, float y, float width, float height, int color) {
    fills.add(new Fill(Rectangle.relative(x, y, width, height), color));
  }

  private static void addOutline(
      List<Fill> fills, float x, float y, float width, float height, int color
  ) {
    fill(fills, x, y, width, 1.0F, color);
    fill(fills, x, y + height - 1.0F, width, 1.0F, color);
    fill(fills, x, y, 1.0F, height, color);
    fill(fills, x + width - 1.0F, y, 1.0F, height, color);
  }

  private static List<Fill> blockFills(float side, int accent) {
    List<Fill> fills = new ArrayList<>(4);
    fill(fills, 0, 0, side, side, 0xFF748194);
    fill(fills, 0, 0, side, 1.0F, accent);
    fill(fills, 0, 1.0F, 1.0F, side - 1.0F, 0xFFB9C3CF);
    fill(fills, side - 1.0F, 1.0F, 1.0F, side - 1.0F, 0xFF202B3B);
    return List.copyOf(fills);
  }

  private static void text(ScreenContext context, RenderableComponent text, float x, float y) {
    context.canvas().submitRenderableComponent(text, x, y, -1, TextRenderingOptions.SHADOW);
  }

  private static void text(ScreenContext context, RenderableComponent text, float x, float y, boolean centered) {
    context.canvas().submitRenderableComponent(text, x, y, -1,
        centered ? CENTERED_SHADOW : TextRenderingOptions.SHADOW);
  }

  private static int opacity(int rgba, float alpha) {
    return ((int) (((rgba >>> 24) & 0xFF) * clamp(alpha)) << 24) | (rgba & 0x00FFFFFF);
  }

  private static float clamp(float value) {
    return Math.max(0.0F, Math.min(1.0F, value));
  }

  private static float ease(float progress) {
    float remaining = 1.0F - progress;
    return 1.0F - remaining * remaining * remaining;
  }

  private static Layout prepare(MatchRecap recap) {
    RenderableComponent brand = bold("EXILE", NamedTextColor.RED);
    RenderableComponent subtitle = translated("exilerecap.hud.title", NamedTextColor.GRAY);
    TextColor resultColor = switch (recap.result()) {
      case VICTORY -> NamedTextColor.GREEN;
      case DEFEAT -> NamedTextColor.RED;
      case DRAW -> NamedTextColor.GOLD;
    };
    RenderableComponent result = boldTranslated(
        "exilerecap.results." + recap.result().name().toLowerCase(Locale.ROOT), resultColor);
    RenderableComponent game = boldTranslated("exilerecap.games." + recap.game(), NamedTextColor.WHITE);
    RenderableComponent mode = translated("exilerecap.modes." + recap.mode(), NamedTextColor.GRAY);
    RenderableComponent durationLabel = translated("exilerecap.hud.duration", NamedTextColor.GRAY);
    RenderableComponent durationValue = RenderableComponent.of(
        Component.translatable("exilerecap.hud.durationValue",
                Component.text(recap.durationSeconds() / 60),
                Component.text(recap.durationSeconds() % 60))
            .color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD));
    RenderableComponent goldLabel = recap.goldEarned() > 0
        ? translated("exilerecap.hud.gold", NamedTextColor.GRAY) : null;
    RenderableComponent goldValue = recap.goldEarned() > 0
        ? bold("+" + recap.goldEarned(), NamedTextColor.GOLD) : null;
    RenderableComponent empty = translated("exilerecap.hud.noStats", NamedTextColor.GRAY);

    List<Map.Entry<String, Integer>> ordered = new ArrayList<>(recap.stats().entrySet());
    ordered.sort(Comparator.comparingInt(entry -> order(recap.game(), entry.getKey())));
    List<Cell> cells = new ArrayList<>(ordered.size());
    float widestCell = 112.0F;
    for (Map.Entry<String, Integer> entry : ordered) {
      RenderableComponent label = translated("exilerecap.stats." + entry.getKey(), NamedTextColor.GRAY);
      RenderableComponent value = bold(Integer.toString(entry.getValue()), NamedTextColor.WHITE);
      widestCell = Math.max(widestCell, Math.max(label.getWidth(), value.getWidth()) + 14.0F);
      cells.add(new Cell(label, value));
    }

    float contentWidth = Math.max(MIN_CONTENT_WIDTH, widestCell * 2.0F + CELL_GAP);
    contentWidth = Math.max(contentWidth, game.getWidth() + 6.0F);
    contentWidth = Math.max(contentWidth,
        brand.getWidth() + subtitle.getWidth() + result.getWidth() + 37.0F);
    if (goldLabel != null) {
      contentWidth = Math.max(contentWidth,
          2.0F * (Math.max(durationLabel.getWidth(), goldLabel.getWidth()) + 12.0F));
    }

    int rows = Math.max(1, (cells.size() + 1) / 2);
    float height = GRID_Y + rows * CELL_HEIGHT + (rows - 1) * CELL_GAP + 15.0F;
    float width = contentWidth + 2.0F * PADDING + 2.0F * FRAME_X;
    float frameWidth = width - 2.0F * FRAME_X;
    float frameHeight = height - 2.0F * FRAME_Y;
    float innerX = FRAME_X + PADDING;
    int gameAccent = accent(recap.game());

    List<Fill> frameFills = new ArrayList<>(16);
    fill(frameFills, FRAME_X + 1.0F, FRAME_Y + 2.0F, frameWidth, frameHeight, 0x65000000);
    fill(frameFills, FRAME_X, FRAME_Y, frameWidth, frameHeight, 0xD10D1522);
    addOutline(frameFills, FRAME_X, FRAME_Y, frameWidth, frameHeight, 0x61788498);
    fill(frameFills, FRAME_X, FRAME_Y, 2.0F, frameHeight, gameAccent);
    fill(frameFills, innerX, 59.0F, contentWidth, 1.0F, 0x5A728196);
    fill(frameFills, innerX, 59.0F, 33.0F, 1.0F, gameAccent);

    float badgeWidth = result.getWidth() + 13.0F;
    float badgeX = FRAME_X + frameWidth - PADDING - badgeWidth;
    fill(frameFills, badgeX, 13.0F, badgeWidth, 15.0F, resultBackground(recap.result()));
    fill(frameFills, badgeX, 13.0F, 2.0F, 15.0F, resultAccent(recap.result()));
    fill(frameFills, innerX, 90.0F, contentWidth, 1.0F, 0x456E7887);

    float cellWidth = (contentWidth - CELL_GAP) / 2.0F;
    List<List<Fill>> cellFills = new ArrayList<>(cells.size());
    for (int i = 0; i < cells.size(); i++) {
      float x = innerX + (i % 2) * (cellWidth + CELL_GAP);
      float y = GRID_Y + (i / 2) * (CELL_HEIGHT + CELL_GAP);
      List<Fill> cell = new ArrayList<>(3);
      fill(cell, x, y, cellWidth, CELL_HEIGHT, 0x93202B3B);
      fill(cell, x, y, cellWidth, 1.0F, 0x4059667C);
      fill(cell, x, y + CELL_HEIGHT - 1.0F, cellWidth, 1.0F, 0x25475465);
      cellFills.add(List.copyOf(cell));
    }

    return new Layout(
        width, height, gameAccent,
        brand, subtitle, game, mode, result, durationLabel, durationValue,
        goldLabel, goldValue, empty, List.copyOf(cells), List.copyOf(frameFills),
        List.copyOf(cellFills), List.of(
            blockFills(5.0F, gameAccent), blockFills(4.0F, gameAccent),
            blockFills(3.0F, gameAccent))
    );
  }

  private static int accent(String game) {
    return switch (game) {
      case "skywars" -> 0xFF49A6D8;
      case "timv", "murdermystery" -> 0xFFAC8BE0;
      case "tntrun", "splegg", "spleef" -> 0xFFE6B867;
      case "tnttag" -> 0xFFF08D65;
      case "blockparty" -> 0xFFE58CCC;
      default -> 0xFFFF5555;
    };
  }

  private static int resultAccent(MatchRecap.Result result) {
    return switch (result) {
      case VICTORY -> 0xFF49B77B;
      case DEFEAT -> 0xFFE06B70;
      case DRAW -> 0xFFE3B865;
    };
  }

  private static int resultBackground(MatchRecap.Result result) {
    return switch (result) {
      case VICTORY -> 0x66449B73;
      case DEFEAT -> 0x667A3B43;
      case DRAW -> 0x666E623D;
    };
  }

  private static int order(String game, String stat) {
    String[] preferred = switch (game) {
      case "bedwars" -> new String[]{"kills", "final_kills", "beds_broken", "deaths"};
      case "skywars" -> new String[]{"kills", "assists", "chests_opened", "placement"};
      case "timv", "murdermystery" -> new String[]{"kills", "chests_opened", "placement"};
      default -> new String[]{"placement", "rounds_survived", "tags", "blocks_broken"};
    };
    for (int i = 0; i < preferred.length; i++) {
      if (preferred[i].equals(stat)) return i;
    }
    return 100;
  }

  private static RenderableComponent translated(String key, TextColor color) {
    return RenderableComponent.of(Component.translatable(key).color(color));
  }

  private static RenderableComponent boldTranslated(String key, TextColor color) {
    return RenderableComponent.of(Component.translatable(key).color(color).decorate(TextDecoration.BOLD));
  }

  private static RenderableComponent bold(String value, TextColor color) {
    return RenderableComponent.of(Component.text(value).color(color).decorate(TextDecoration.BOLD));
  }

  private static Map<String, Integer> previewStats() {
    Map<String, Integer> result = new LinkedHashMap<>();
    result.put("kills", 7);
    result.put("final_kills", 3);
    result.put("beds_broken", 1);
    return result;
  }

  private record Cell(RenderableComponent label, RenderableComponent value) {
  }

  private record Fill(Rectangle rectangle, int color) {
  }

  private record Layout(
      float width, float height, int accent,
      RenderableComponent brand, RenderableComponent subtitle, RenderableComponent game,
      RenderableComponent mode, RenderableComponent result,
      RenderableComponent durationLabel, RenderableComponent durationValue,
      RenderableComponent goldLabel, RenderableComponent goldValue,
      RenderableComponent empty, List<Cell> cells,
      List<Fill> frameFills, List<List<Fill>> cellFills, List<List<Fill>> blockFills
  ) {
  }
}
