package one.cax.textractor.text;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import one.cax.textractor.text.DocumentExtractionException;
import one.cax.textractor.datamodel.XDoc;
import one.cax.textractor.text.ExtractorEngine;
import one.cax.textractor.utilities.NameUtils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

class ExtractorEngineTest {

    private MeterRegistry meterRegistry;

    @BeforeEach
    public void setUp() {
        meterRegistry = new SimpleMeterRegistry();
    }

    @Test
    void testExtractTextFromPDF_NotNull() throws DocumentExtractionException, IOException, JSONException {
        ExtractorEngine extractorEngine = new ExtractorEngine(meterRegistry);
        byte[] fileInBytes = createPdf("test data");
        FileOutputStream fos = new FileOutputStream("test.pdf");
        fos.write(fileInBytes);
        fos.close();
        File f = new File("test.pdf");
        JSONObject jsonDoc = extractorEngine.extractTextFromPDF(f.getPath());
        XDoc xDoc = XDoc.fromText(jsonDoc.toString());
        assertNotNull(xDoc.getDocTitle());
        assertNotNull(xDoc.getId().toString());

    }

    @Test
    void testExtractTextFromPDF_Null() {
        ExtractorEngine extractorEngine = new ExtractorEngine(meterRegistry);
        byte[] fileInBytes = null;
        assertThrows(DocumentExtractionException.class, () -> extractorEngine.extractTextFromPDF(fileInBytes));
    }

    @Test
    void testExtractTextFromPDF_Empty() throws DocumentExtractionException {
        ExtractorEngine extractorEngine = new ExtractorEngine(meterRegistry);
        byte[] fileInBytes = createPdf("");
        XDoc xDoc = extractorEngine.extractTextFromPDF(fileInBytes);
        assertEquals(" ", xDoc.getDocTitle());

    }

    @Test
    void testExtractTextFromPDF_Large() throws DocumentExtractionException {
        ExtractorEngine extractorEngine = new ExtractorEngine(meterRegistry);
        byte[] fileInBytes = createPdf("a".repeat(1000000));
        XDoc xDoc = extractorEngine.extractTextFromPDF(fileInBytes);
        assertNotNull(xDoc.getDocTitle());

    }

    @Test
    void testExtractTextFromPDF_Invalid() {
        ExtractorEngine extractorEngine = new ExtractorEngine(meterRegistry);
        byte[] fileInBytes = "This is not a valid PDF".getBytes();
        assertThrows(DocumentExtractionException.class, () -> extractorEngine.extractTextFromPDF(fileInBytes));
    }

    public static byte[] createPdf(String content) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(font, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText(content);
            contentStream.endText();
            contentStream.close();

            document.save(output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output.toByteArray();
    }

    public static JSONObject getJsonObject() throws JSONException {
        JSONObject jsonDoc = new JSONObject();
        jsonDoc.put(NameUtils.DOC_TITLE, "Test document title");
        jsonDoc.put(NameUtils.DOC_TOTAL_PAGES, 1);

        var xPage = new XDoc.XPage();
        xPage.setPageNumber(1);
        xPage.setContent("Test document content");
        var jsonArray = new JSONArray();
        jsonArray.put(xPage.toJSON());
        jsonDoc.put(NameUtils.DOC_PAGES, jsonArray);
        return jsonDoc;
    }

    /**
     * Get the URI for a search request.
     *
     * @param sessionId
     * @param action
     * @return
     */
    public static String getURIForSearch(String sessionId, String action) {
        return String.format("/search/%s/%s", sessionId, action);
    }

}