package com.truward.metrics.json.internal.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * An interface to the memory cache where metrics content may live.
 * <p>Implementations of this class should be thread safe</p>
 * <p>THIS CLASS IS NOT A PART OF THE PUBLIC API.</p>
 *
 * @author Alexander Shabanov
 */
public interface RecordCache {

  /**
   * Tells underlying implementation that this map has been discarded and
   * can be reused later.
   *
   * @param value Discarded map instance.
   */
  void take(@Nonnull Map<String, Object> value);

  /**
   * Tries to fetch a map instance from the object cache.
   * Guarantees that the returned map will be empty.
   *
   * @return Retrieved map instance from the object cache or null if cache is empty/cleared.
   */
  @Nullable
  Map<String, Object> fetch();
}
