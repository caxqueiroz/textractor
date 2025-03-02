package one.cax.textractor.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Document representation with pages and content.
 */
public class XDoc {

    private final UUID fileId;
    private int numberPages;
    private final List<Page> pages;
    
    /**
     * Default constructor for Jackson deserialization.
     */
    public XDoc() {
        this.fileId = UUID.randomUUID();
        this.pages = new ArrayList<>();
    }
    
    /**
     * Constructor for XDoc.
     * @param fileId Unique identifier for the document
     */
    public XDoc(UUID fileId) {
        this.fileId = fileId;
        this.pages = new ArrayList<>();
    }

    /**
     * Add a page to the document.
     * @param page The page to add
     */
    public void addPage(Page page) {
        pages.add(page);
        numberPages++;
    }

    /**
     * Get a specific page by page number.
     * @param pageNumber The page number (0-based index)
     * @return The page
     */
    public Page getPage(int pageNumber) {
        return pages.get(pageNumber);
    }
    
    /**
     * Get the document's unique ID.
     * @return The file ID
     */
    public UUID getFileId() {
        return fileId;
    }
    
    /**
     * Get the number of pages in the document.
     * @return Number of pages
     */
    public int getNumberPages() {
        return numberPages;
    }
    
    /**
     * Get all pages in the document.
     * @return List of pages
     */
    public List<Page> getPages() {
        return pages;
    }
    
    /**
     * Convert the XDoc to a JsonNode object representation.
     * @return JsonNode representing the document and its pages
     */
    public JsonNode toJsonNode() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.valueToTree(this);
    }
    
    /**
     * Convert the XDoc to a JSON string representation.
     * @return JSON string of the document and its pages
     * @throws JsonProcessingException if there's an error during JSON serialization
     */
    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
    
    /**
     * Convert the XDoc to a pretty-printed JSON string representation.
     * @return Formatted JSON string of the document and its pages
     * @throws JsonProcessingException if there's an error during JSON serialization
     */
    public String toPrettyJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }

    /**
     * Page representation with content.
     */
    public static class Page {
        @JsonProperty("page_number")
        private int pageNumber;
        private String content;

        /**
         * Default constructor for Jackson deserialization.
         */
        public Page() {
        }
        
        /**
         * Constructor for Page.
         * @param content The text content of the page
         */
        public Page(int pageNumber, String content) {  
            this.pageNumber = pageNumber;
            this.content = content;
        }

        /**
         * Get the page number.
         * @return The page number
         */
        public int getPageNumber() {
            return pageNumber;
        }   

        /**
         * Set the page number.
         * @param pageNumber The page number
         */
        public void setPageNumber(int pageNumber) { 
            this.pageNumber = pageNumber;
        }

        /**
         * Get the content of the page.
         * @return The text content
         */
        public String getContent() {
            return content;
        }

        /**
         * Set the content of the page.
         * @param content The text content
         */
        public void setContent(String content) {
            this.content = content;
        }
    }
}
