package com.truward.metrics.reader;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * Represents an abstraction over metrics reader that can read metrics entries in a form of a map from
 * the certain source.
 *
 * @author Alexander Shabanov
 */
public interface MetricsReader extends Closeable {

  /**
   * Reads next metrics record from the current stream.
   *
   * @return Deserialized metrics instance, null if there are no more entries in the current stream
   * @throws IOException On I/O error
   */
  @Nullable
  Map<String, ?> readNext() throws IOException;
}
