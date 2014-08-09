package com.truward.metrics.json.internal.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * An implementation that does not attempt to store any properties and never responds with non-null object taken
 * from the cache.
 *
 * @author Alexander Shabanov
 */
public final class EmptyRecordCache implements RecordCache {
  private static final EmptyRecordCache INSTANCE = new EmptyRecordCache();

  private EmptyRecordCache() {
  }

  @Nonnull
  public static EmptyRecordCache getInstance() {
    return INSTANCE;
  }

  @Override
  public void take(@Nonnull Map<String, Object> value) {
    // do nothing
  }

  @Nullable
  @Override
  public Map<String, Object> fetch() {
    return null;
  }
}
