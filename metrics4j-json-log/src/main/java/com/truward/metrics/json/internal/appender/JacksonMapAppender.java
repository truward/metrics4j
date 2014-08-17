package com.truward.metrics.json.internal.appender;

import com.truward.metrics.json.internal.cache.RecordCache;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Jackson-based map appender.
 * UTF-8 encoding is used to write JSON values to the underlying output stream.
 * <p>THIS CLASS IS NOT A PART OF THE PUBLIC API.</p>
 *
 * @author Alexander Shabanov
 */
public final class JacksonMapAppender extends AbstractJacksonMapAppender {
  private OutputStream outputStream;

  public JacksonMapAppender(@Nonnull OutputStream outputStream, @Nonnull RecordCache recordCache) {
    super(recordCache);
    this.outputStream = outputStream;
  }

  @Nonnull
  @Override
  protected OutputStream getOutputStream() {
    final OutputStream result = outputStream;
    if (result == null) {
      throw new IllegalStateException("outputStream is null"); // shouldn't happen
    }
    return result;
  }

  @Override
  protected void onClose() throws IOException {
    if (outputStream != null) {
      outputStream.close();
      outputStream = null;
    }
  }
}
