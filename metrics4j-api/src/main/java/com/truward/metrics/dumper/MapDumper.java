package com.truward.metrics.dumper;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Represents an abstraction over certain dumper that takes care about writing certain map into some storage.
 *
 * @author Alexander Shabanov
 */
public interface MapDumper {

  /**
   * Writes a map into the associated storage.
   * <p>
   * Each value in the given map should be either of primitive type, or BigDecimal or String-to-Object map or
   * list of objects.
   * Each object in the aforementioned collections should be of any types described above.
   * </p>
   * <p>
   * An exception thrown will be suppressed and logged. This method guarantees to not to throw any exception related
   * to unsuccessful I/O operation.
   * </p>
   * <p>
   * Each metrics object should invoke this method only once and then clear reference to the passed map, so
   * the given map instance might be possibly reused when the other metrics entry is created.
   * </p>
   *
   * @param properties A map that should be written into the corresponding storage.
   */
  void write(@Nonnull Map<String, Object> properties);

  /**
   * Records an error about duplicate entry in the metrics instance - it might override something important which
   * was written in the map.
   * The only reason this method is here and not in separate interface to minimize amount of fields in the metrics object.
   *
   * @param source Partially constructed contents of the metrics record
   * @param key    Name of an entry in the given metrics record which is duplicated
   */
  void reportDuplicateEntry(@Nonnull Map<String, Object> source, @Nonnull String key);
}
