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
        asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();
        
        // Add custom escapes for problematic characters if needed
        // For example, paragraph separator (U+2029) that causes issues
        asciiEscapes[0x2029] = CharacterEscapes.ESCAPE_CUSTOM;
    }
    
    @Override
    public int[] getEscapeCodesForAscii() {
        return asciiEscapes;
    }
    
    @Override
    public SerializableString getEscapeSequence(int ch) {
        // Handle the paragraph separator character specifically
        if (ch == 0x2029) {
            return new SerializedString("\\u2029");
        }
        
        // For other non-ASCII characters that need escaping
        if (Character.isISOControl(ch) || (ch >= 0x7F && ch <= 0x9F)) {
            String hex = Integer.toHexString(ch);
            return new SerializedString("\\u" + "0000".substring(hex.length()) + hex);
        }
        
        // Let Jackson handle standard escaping for other characters
        return null;
    }
}
