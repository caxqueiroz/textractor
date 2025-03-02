package one.cax.textractor.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import one.cax.textractor.utilities.NameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Document representation with pages and content.
 */
public class XDoc {

    private final UUID fileId;
    private int numberPages;
    /* The pages of the document */
    private AtomicReference<List<XPage>> pages = new AtomicReference<>(new ArrayList<>());

    /* The title of the document */
    private String docTitle;

    /* The filename of the document */
    private String filename;


    /* The metadata of the document */
    private HashMap<String, Object> metadata;
    
    /**
     * Default constructor for Jackson deserialization.
     */
    public XDoc() {
        this.fileId = UUID.randomUUID();

    }

    public int getTotalPages() {
        return this.pages.get().size();
    }
    /**
     * Constructor for XDoc.
     * @param fileId Unique identifier for the document
     */
    public XDoc(UUID fileId) {
        this.fileId = fileId;
    }

    /**
     * Add a page to the document.
     * @param page The page to add
     */
    public void addPage(XPage page) {
        pages.get().add(page);
        numberPages++;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    /**
     * Get a specific page by page number.
     * @param pageNumber The page number (0-based index)
     * @return The page
     */
    public XPage getPage(int pageNumber) {

        return pages.get().get(pageNumber);
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

    public static XDoc fromText(String document) throws JSONException {
        var json = new JSONObject(document);
        var xDoc = new XDoc();
        xDoc.setDocTitle(json.getString(NameUtils.DOC_TITLE));
        xDoc.setPages(XPage.fromJSONArray(json.getJSONArray(NameUtils.DOC_PAGES)));
        return xDoc;
    }
    /**
     * Page representation with content.
     */
    public static class XPage {
        @JsonProperty("page_number")
        private int pageNumber;
        private String content;

        /**
         * Default constructor for Jackson deserialization.
         */
        public XPage() {
        }
        
        /**
         * Constructor for Page.
         * @param content The text content of the page
         */
        public XPage(int pageNumber, String content) {
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

        public JSONObject toJSON() {
            var json = new JSONObject();
            json.put("page_number", pageNumber);
            json.put("content", content);
            return json;

        }
    }
}
