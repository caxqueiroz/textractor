package one.cax.textractor.datamodel;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class XDocTest {

    @Test
    void testAddPage() {
        // Arrange
        UUID fileId = UUID.randomUUID();
        XDoc xDoc = new XDoc(fileId);
        XDoc.Page page = new XDoc.Page(1, "Test content");
        
        // Act
        xDoc.addPage(page);
        
        // Assert
        assertEquals(1, xDoc.getNumberPages());
        assertEquals(page, xDoc.getPage(0));
    }

    @Test
    void testGetPage() {
        // Arrange
        UUID fileId = UUID.randomUUID();
        XDoc xDoc = new XDoc(fileId);
        XDoc.Page page1 = new XDoc.Page(1, "Page 1 content");
        XDoc.Page page2 = new XDoc.Page(2, "Page 2 content");
        
        // Act
        xDoc.addPage(page1);
        xDoc.addPage(page2);
        
        // Assert
        assertEquals(page1, xDoc.getPage(0));
        assertEquals(page2, xDoc.getPage(1));
    }

    @Test
    void testGetFileId() {
        // Arrange
        UUID fileId = UUID.randomUUID();
        XDoc xDoc = new XDoc(fileId);
        
        // Act & Assert
        assertEquals(fileId, xDoc.getFileId());
    }

    @Test
    void testGetNumberPages() {
        // Arrange
        UUID fileId = UUID.randomUUID();
        XDoc xDoc = new XDoc(fileId);
        
        // Act
        xDoc.addPage(new XDoc.Page(1, "Page 1"));
        xDoc.addPage(new XDoc.Page(2, "Page 2"));
        xDoc.addPage(new XDoc.Page(3, "Page 3"));
        
        // Assert
        assertEquals(3, xDoc.getNumberPages());
    }

    @Test
    void testGetPages() {
        // Arrange
        UUID fileId = UUID.randomUUID();
        XDoc xDoc = new XDoc(fileId);
        XDoc.Page page1 = new XDoc.Page(1, "Page 1 content");
        XDoc.Page page2 = new XDoc.Page(2, "Page 2 content");
        
        // Act
        xDoc.addPage(page1);
        xDoc.addPage(page2);
        
        // Assert
        assertEquals(2, xDoc.getPages().size());
        assertTrue(xDoc.getPages().contains(page1));
        assertTrue(xDoc.getPages().contains(page2));
    }

    @Test
    void testToJson() throws Exception {
        // Arrange
        UUID fileId = UUID.randomUUID();
        XDoc xDoc = new XDoc(fileId);
        xDoc.addPage(new XDoc.Page(1, "Test content"));
        
        // Act
        String json = xDoc.toJson();
        
        // Assert
        assertNotNull(json);
        assertTrue(json.contains(fileId.toString()));
        assertTrue(json.contains("Test content"));
        assertTrue(json.contains("page_number"));
    }

    @Test
    void testToPrettyJson() throws Exception {
        // Arrange
        UUID fileId = UUID.randomUUID();
        XDoc xDoc = new XDoc(fileId);
        xDoc.addPage(new XDoc.Page(1, "Test content"));
        
        // Act
        String prettyJson = xDoc.toPrettyJson();
        
        // Assert
        assertNotNull(prettyJson);
        assertTrue(prettyJson.contains(fileId.toString()));
        assertTrue(prettyJson.contains("Test content"));
        assertTrue(prettyJson.contains("page_number"));
        // Pretty JSON should contain newlines
        assertTrue(prettyJson.contains("\n"));
    }

    @Test
    void testToJsonNode() {
        // Arrange
        UUID fileId = UUID.randomUUID();
        XDoc xDoc = new XDoc(fileId);
        xDoc.addPage(new XDoc.Page(1, "Test content"));
        
        // Act
        JsonNode jsonNode = xDoc.toJsonNode();
        
        // Assert
        assertNotNull(jsonNode);
        assertEquals(fileId.toString(), jsonNode.get("fileId").asText());
        assertEquals(1, jsonNode.get("numberPages").asInt());
        assertEquals(1, jsonNode.get("pages").size());
        assertEquals(1, jsonNode.get("pages").get(0).get("page_number").asInt());
        assertEquals("Test content", jsonNode.get("pages").get(0).get("content").asText());
    }

    @Test
    void testPageGettersAndSetters() {
        // Arrange
        XDoc.Page page = new XDoc.Page(1, "Initial content");
        
        // Act & Assert
        assertEquals(1, page.getPageNumber());
        assertEquals("Initial content", page.getContent());
        
        // Act
        page.setPageNumber(2);
        page.setContent("Updated content");
        
        // Assert
        assertEquals(2, page.getPageNumber());
        assertEquals("Updated content", page.getContent());
    }

    @Test
    void testPageDefaultConstructor() {
        // Arrange & Act
        XDoc.Page page = new XDoc.Page();
        
        // Assert
        assertEquals(0, page.getPageNumber());
        assertNull(page.getContent());
        
        // Act
        page.setPageNumber(5);
        page.setContent("Content set after construction");
        
        // Assert
        assertEquals(5, page.getPageNumber());
        assertEquals("Content set after construction", page.getContent());
    }
}
