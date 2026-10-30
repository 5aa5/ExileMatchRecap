package net.playexile.recap.core;

import net.playexile.recap.api.MatchRecap;

public final class RecapState {
  private volatile MatchRecap current;
  private volatile MatchRecap last;
  private volatile long shownAtNanos;
  private volatile long visibleUntilNanos;

  public synchronized void show(MatchRecap recap, int displaySeconds) {
    MatchRecap previous = this.last;
    if (previous != null && previous.matchId().equals(recap.matchId())) {
      return;
    }

    long now = System.nanoTime();
    this.shownAtNanos = now;
    this.visibleUntilNanos = now + (long) Math.clamp(displaySeconds, 4, 20) * 1_000_000_000L;
    this.last = recap;
    this.current = recap;
  }

  public MatchRecap active() {
    MatchRecap recap = this.current;
    return recap != null && System.nanoTime() - this.visibleUntilNanos < 0 ? recap : null;
  }

  public long elapsedNanos(long now) {
    return Math.max(0L, now - this.shownAtNanos);
  }

  public long remainingNanos(long now) {
    return Math.max(0L, this.visibleUntilNanos - now);
  }

  public MatchRecap last() {
    return this.last;
  }

  public synchronized void dismiss() {
    this.current = null;
    this.shownAtNanos = 0L;
    this.visibleUntilNanos = 0L;
  }
}
