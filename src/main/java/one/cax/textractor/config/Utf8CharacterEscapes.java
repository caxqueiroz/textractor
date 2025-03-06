package one.cax.textractor.config;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;

/**
 * Custom character escapes for Jackson to ensure proper UTF-8 encoding.
 * This class helps prevent character encoding issues when serializing JSON.
 */
public class Utf8CharacterEscapes extends CharacterEscapes {
    private static final long serialVersionUID = 1L;
    
    private final int[] asciiEscapes;
    
    public Utf8CharacterEscapes() {
        // Start with the standard ASCII escapes
        // This array has exactly 128 entries for ASCII characters (0-127)
        asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();
        
        // Note: We don't modify the asciiEscapes array for non-ASCII characters
        // like U+2028 and U+2029 as they would cause ArrayIndexOutOfBoundsException
        // Instead, we handle them in getEscapeSequence method
    }
    
    @Override
    public int[] getEscapeCodesForAscii() {
        return asciiEscapes;
    }
    
    @Override
    public SerializableString getEscapeSequence(int ch) {
        // Handle specific Unicode characters that need escaping in JSON
        // These are outside ASCII range so we handle them here, not in the asciiEscapes array
        if (ch == 0x2028) {
            return new SerializedString("\\u2028"); // Line separator
        } else if (ch == 0x2029) {
            return new SerializedString("\\u2029"); // Paragraph separator
        }
        
        // Handle control characters and other problematic Unicode characters
        if (ch > 0x7F && (Character.isISOControl(ch) || 
            (ch >= 0x7F && ch <= 0x9F) ||
            Character.isSupplementaryCodePoint(ch))) {
            String hex = Integer.toHexString(ch);
            // Ensure proper padding for the hex value
            while (hex.length() < 4) {
                hex = "0" + hex;
            }
            return new SerializedString("\\u" + hex);
        }
        
        // Let Jackson handle standard escaping for other characters
        return null;
    }
}
