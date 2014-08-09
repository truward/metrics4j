package com.truward.metrics.json.reader;

import com.truward.metrics.reader.MetricsReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Represents metrics reader, that reads JSON-encoded metrics from the given UTF-8-encoded input stream.
 * <p>
 * Instances of this class are not thread safe.
 * </p>
 * <p>
 * It doesn't make sense to pass buffered {@code InputStream} instance to this class as
 * it already has buffering capabilities, however if given input stream might often return less bytes than requested
 * and multiple read requests are required to fully read metrics entry into the source byte buffer, it might result
 * in a stack overflow. So, if metrics entry is much larger than usual portion of data returned from the input stream
 * it is advised to pass buffered input stream.
 * </p>
 * <p>
 * Internal buffer will be reallocated if a considerably big metrics record will occur in the given stream,
 * up to the given maxim buffer size. If certain metrics record exceeds maximum buffer size, an exception will
 * be thrown and reader will be reset.
 * </p>
 *
 * @author Alexander Shabanov
 */
public abstract class AbstractJsonMetricsReader implements MetricsReader {
  /**
   * Default size of the internally maintained buffer, 4 Kilobytes.
   */
  public static final int DEFAULT_BUFFER_SIZE = 4 * 1024;

  /**
   * Default maximum size of the internally maintained buffer, 1 Megabyte.
   */
  public static final int DEFAULT_MAX_BUFFER_SIZE = 1024 * 1024;

  private byte[] buffer;
  private int pos = 0;
  private int last = 0;
  private final int maxBufferSize;

  private InputStream inputStream;


  /**
   * Creates an instance of the metrics reader object
   *
   * @param inputStream       Source input stream
   * @param initialBufferSize Initial size of the internal buffer, usually {@link #DEFAULT_BUFFER_SIZE}
   * @param maxBufferSize     Maximum size of the internal buffer, that no entry in the given input stream
   *                          should exceed. Usually {@link #DEFAULT_MAX_BUFFER_SIZE}
   */
  public AbstractJsonMetricsReader(@Nonnull InputStream inputStream, int initialBufferSize, int maxBufferSize) {
    if (initialBufferSize <= 0) {
      throw new IllegalArgumentException("initialBufferSize should be greater than zero");
    }

    if (maxBufferSize <= 0) {
      throw new IllegalArgumentException("maxBufferSize should be greater than zero");
    }

    //noinspection ConstantConditions
    if (inputStream == null) {
      throw new NullPointerException("inputStream can't be null");
    }

    this.inputStream = inputStream;
    this.buffer = new byte[initialBufferSize];
    this.maxBufferSize = maxBufferSize;
  }

  public AbstractJsonMetricsReader(@Nonnull InputStream inputStream) {
    this(inputStream, DEFAULT_BUFFER_SIZE, DEFAULT_MAX_BUFFER_SIZE);
  }

  @Nullable
  @Override
  public final Map<String, ?> readNext() throws IOException {
    if (buffer == null || inputStream == null) {
      throw new IllegalStateException("Reader closed, can't read another metric");
    }

    return readFromPos();
  }

  /**
   * Abstract method, that should delegate json parsing in the given array region to the corresponding
   * json parser.
   *
   * @param arr      Byte buffer, that contains json to parse
   * @param startPos Start of the object entry, points to the open curly brace in the given buffer
   * @param len      Count of bytes
   * @return Deserialized JSON object
   * @throws IOException On I/O or JSON parsing error
   */
  @Nonnull
  protected abstract Map<String, ?> parseJson(@Nonnull byte[] arr, int startPos, int len) throws IOException;

  /**
   * Closes current metrics reader with the associated input stream.
   * <p/>
   * {@inheritDoc}
   */
  @Override
  public void close() throws IOException {
    if (inputStream != null) {
      inputStream.close();
      inputStream = null;
    }

    if (buffer != null) {
      buffer = null;
    }
  }

  //
  // Private
  //

  @Nullable
  private Map<String, ?> readFromPos() throws IOException {
    assert buffer.length > 0;
    if (last < 0) {
      return null; // end of the buffer
    }

    // try to find next record: skip anything before open curly brace
    while (pos < last && buffer[pos] != '{') {
      ++pos;
    }

    // 'pos == last' means that we haven't met open curly brace and we're at the end of the current stream,
    // we need to read more into the current buffer
    if (last == pos) {
      // last equals to buffer size, try to read from file into this buffer again
      pos = 0; // reset pos
      last = inputStream.read(buffer);
      return readFromPos();
    }

    // ok: open curly brace found, now find closing curly brace
    final int startPos = pos; // record start of the metrics record
    ++pos; // go to the symbol after the very first brace
    int braceCount = 1; // count found brace as first one
    for (; pos < last; ++pos) {
      final byte ch = buffer[pos];
      switch (ch) {
        case '{':
          ++braceCount;
          break;
        case '}':
          --braceCount;
          break;
      }
      if (braceCount == 0) {
        // found end curly brace
        ++pos; // go to the symbol after this curly brace
        return parseJson(buffer, startPos, pos - startPos);
      }
    }

    return continueReadingFromStartPos(startPos); // unbalanced braces at the end of current buffer, try to read more
  }

  @Nullable
  private Map<String, ?> continueReadingFromStartPos(int startPos) throws IOException {
    // if buffer is too small to fit current json entry, it needs to be reallocated
    if (startPos == 0 && last == buffer.length) {
      // if max buffer size exceeded, reset and give up
      if (buffer.length >= maxBufferSize) {
        pos = 0;
        last = -1;
        throw new IOException("Current JSON entry is too large or malformed, exceeding " +
            "maxBufferSize=" + maxBufferSize + " byte(s)");
      }

      // calculate size of the newly allocated byte buffer
      int newBufferSize = buffer.length * 2;
      if (newBufferSize > maxBufferSize) {
        newBufferSize = maxBufferSize;
      }

      // allocate a new byte buffer and move older content into the newly allocated buffer, discard older buffer
      final byte[] newBuffer = new byte[newBufferSize];
      System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
      buffer = newBuffer;
      pos = startPos; // TODO: more optimal way, this approach instructs to rescan current metrics entry
    } else if (last == buffer.length) {
      // current metrics record starts near the end of the current buffer, move it at the beginning of the buffer
      assert startPos > 0 && startPos < pos;
      final int count = last - startPos;
      System.arraycopy(buffer, startPos, buffer, 0, count);
      pos = 0;
      last = count;
    }

    // paranoid check: at this point we should be able to read another chunk of bytes into the current buffer
    if (last >= buffer.length) {
      throw new IllegalStateException("Internal error: should be able to read another chunk of bytes into the buffer");
    }

    final int read = inputStream.read(buffer, last, buffer.length - last);
    if (read <= 0) {
      pos = 0;
      last = -1;
      throw new IOException("End of stream reached in the middle of the metrics record, " +
          "malformed JSON record at the end of the stream");
    }

    last += read; // update end position of the last record
    return readFromPos();
  }
}