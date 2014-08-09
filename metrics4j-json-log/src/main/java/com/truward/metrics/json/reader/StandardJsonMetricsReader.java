package com.truward.metrics.json.reader;

import com.fasterxml.jackson.core.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Represents a standard implementation of JSON-based metrics reader.
 * <p>
 * Implementation is based on core Jackson classes.
 * </p>
 *
 * @author Alexander Shabanov
 */
public class StandardJsonMetricsReader extends AbstractJsonMetricsReader {
  private final JsonFactory factory = new JsonFactory();

  /**
   * Creates an instance of the metrics reader object
   *
   * @param inputStream       Source input stream
   * @param initialBufferSize Initial size of the internal buffer, usually {@link #DEFAULT_BUFFER_SIZE}
   * @param maxBufferSize     Maximum size of the internal buffer, that no entry in the given input stream
   *                          should exceed. Usually {@link #DEFAULT_MAX_BUFFER_SIZE}
   */
  public StandardJsonMetricsReader(@Nonnull InputStream inputStream, int initialBufferSize, int maxBufferSize) {
    super(inputStream, initialBufferSize, maxBufferSize);
  }

  public StandardJsonMetricsReader(@Nonnull InputStream inputStream) {
    super(inputStream);
  }

  @Nonnull
  @Override
  protected Map<String, ?> parseJson(@Nonnull byte[] arr, int startPos, int len) throws IOException {
    final JsonParser jp = factory.createParser(arr, startPos, len);
    jp.nextToken();
    return parseMap(jp);
  }

  //
  // Private
  //

  @Nonnull
  private Map<String, ?> parseMap(@Nonnull JsonParser jp) throws IOException {
    JsonToken token = jp.getCurrentToken();
    if (token != JsonToken.START_OBJECT) {
      throw new JsonParseException("Map expected", jp.getCurrentLocation());
    }

    final Map<String, Object> result = new HashMap<>(20);
    for (token = jp.nextToken(); token != JsonToken.END_OBJECT; token = jp.nextToken()) {
      if (token != JsonToken.FIELD_NAME) {
        throw new JsonParseException("Field name expected", jp.getCurrentLocation());
      }

      final String fieldName = jp.getText();
      jp.nextToken();
      result.put(fieldName, parseObject(jp));
    }
    return result.isEmpty() ? Collections.<String, Object>emptyMap() : result;
  }

  private List<?> parseArray(@Nonnull JsonParser jp) throws IOException {
    JsonToken token = jp.getCurrentToken();
    if (token != JsonToken.START_ARRAY) {
      throw new JsonParseException("Array expected", jp.getCurrentLocation());
    }

    final List<Object> result = new ArrayList<>();
    for (token = jp.nextToken(); token != JsonToken.END_ARRAY; token = jp.nextToken()) {
      result.add(parseObject(jp));
    }
    return result.isEmpty() ? Collections.emptyList() : result;
  }

  @Nullable
  private Object parseObject(@Nonnull JsonParser jp) throws IOException {
    JsonToken token = jp.getCurrentToken();
    switch (token) {
      case VALUE_NULL:
        return null;

      case VALUE_TRUE:
      case VALUE_FALSE:
        return jp.getBooleanValue();

      case VALUE_NUMBER_INT:
      case VALUE_NUMBER_FLOAT:
        switch (jp.getNumberType()) {
          case INT:
            return jp.getIntValue();
          case LONG:
            return jp.getLongValue();
          case FLOAT:
            return jp.getFloatValue();
          case DOUBLE:
            return jp.getDoubleValue();
          case BIG_DECIMAL:
            return jp.getDecimalValue();
          case BIG_INTEGER:
            return jp.getBigIntegerValue();
          default:
            throw new JsonParseException("Unknown numberType=" + jp.getNumberType(), jp.getCurrentLocation());
        }

      case VALUE_STRING:
        return jp.getText();

      case START_OBJECT:
        return parseMap(jp);

      case START_ARRAY:
        return parseArray(jp);

      default:
        throw new JsonParseException("Unexpected token=" + token, jp.getCurrentLocation());
    }
  }
}
