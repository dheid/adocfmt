package org.drjekyll.adocfmt;

/**
 * Thrown when a document contains a mixture of incompatible line ending styles.
 *
 * <p>The formatter requires all line endings in a single document to be of the same style (LF,
 * CRLF, or CR). Processing a document that mixes styles throws this exception.
 */
public class UnsupportedLineEndingException extends Exception {

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message a human-readable description of the problem
   */
  public UnsupportedLineEndingException(String message) {
    super(message);
  }
}
