package one.cax.textractor.text;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;


import java.io.*;

/**
 * ExtractorEngine is responsible for extracting text from PDF documents.
 * <p>
 * This service provides methods to extract text from PDF files, either by file path
 * or directly from byte arrays. It uses Apache PDFBox for PDF processing and includes
 * metrics for monitoring extraction performance.
 * <p>
 * Key functionalities:
 * - Extracting text from PDF files specified by file path
 * - Extracting text from PDF files provided as byte arrays
 * - Measuring extraction time and success rate using Micrometer metrics
 * <p>
 * The extracted text is returned as either a JSONObject or an XDoc object,
 * depending on the method used.
 */
@Service
public class ExtractorEngine {

    private final Timer extractTextFromTimer;
    private final Counter successfulExtractsCounter;


    /**
     * Create a new ExtractorEngine.
     *
     * @param meterRegistry The meter registry to register metrics with.
     */
    public ExtractorEngine(MeterRegistry meterRegistry) {
        this.extractTextFromTimer = Timer
                .builder("ExtractText")
                .description("Time taken to extract text from PDF")
                .register(meterRegistry);

        this.successfulExtractsCounter = Counter
                .builder("successfulExtracts")
                .description("Number of successful text extracts from PDF")
                .register(meterRegistry);
    }

    /**
     * Extract text from a PDF file. The extracted text is returned as a JSON object.
     *
     * @param inputFile The path to the PDF file to extract text from.
     * @return A JSON object containing the extracted text.
     * @throws DocumentExtractionException If an error occurs while reading the document.
     */
    public JSONObject extractTextFromPDF(String inputFile) throws DocumentExtractionException {
        try {
            return extractTextFromTimer.recordCallable(() -> {
                JSONObject result = doExtractTextFromPDF(inputFile);
                successfulExtractsCounter.increment();
                return result;
            });
        } catch (Exception e) {
            throw new DocumentExtractionException("Error extracting text from PDF", e);
        }
    }

    /**
     * Extracts text from office docs
     * @param inputFile - the path to the office doc
     * @return the extracted text in json format.
     * @throws DocumentExtractionException throws an exception if an error occurs
     */
    public JSONObject extractTextFromOfficeDocs(String inputFile) throws DocumentExtractionException {
        try {
            File f = new File(inputFile);
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(f);

            File pdfFile = new File(f.getParent(), f.getName().replace(".docx", ".pdf"));
            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                Docx4J.toPDF(wordMLPackage, fos);
            }
            return extractTextFromPDF(pdfFile.getPath());

        } catch (Docx4JException | IOException e) {
            throw new DocumentExtractionException("Failed to convert DOCX to PDF", e);
        }
    }

    /**
     * Extract text from a PDF file. The extracted text is returned as a JSON object.
     * The JSON object contains the following fields:
     * - doc_title: The title of the document.
     * - filename: The name of the file.
     *
     * @param inputFile The path to the PDF file to extract text from.
     * @return A JSON object containing the extracted text.
     * @throws IOException If an error occurs while reading the document.
     */
    private JSONObject doExtractTextFromPDF(String inputFile) throws IOException, JSONException {
        JSONObject doc = new JSONObject();

        File f = new File(inputFile);
        String fileName = f.getName();
        PDDocument pdDocument = Loader.loadPDF(f);
        doc.put("doc_title", getTitle(pdDocument));
        doc.put("filename", fileName);

        PDFTextStripper pdfStripper = new PDFTextStripper();
        int nPages = pdDocument.getNumberOfPages();
        doc.put("total_pages", String.valueOf(nPages));
        JSONArray pages = new JSONArray();


        for (int i = 1; i <= nPages; i++) {
            pdfStripper.setStartPage(i);
            pdfStripper.setEndPage(i);
            String pageText = pdfStripper.getText(pdDocument);
            JSONObject page = new JSONObject();
            page.put("page_number", String.valueOf(i));
            page.put("page_text", pageText);
            pages.put(page);
        }
        doc.put("pages", pages);

        return doc;
    }

    /**
     * Get the title of a PDF document.
     * If the document has a title, it is returned. Otherwise, the title of the first page is returned.
     *
     * @param pdDocument The PDF document to get the title from.
     * @return The title of the PDF document.
     * @throws IOException If an error occurs while reading the document.
     */
    private String getTitle(PDDocument pdDocument) throws IOException {
        String title = pdDocument.getDocumentInformation().getTitle();
        if (title != null && !title.isEmpty()) {
            return title.replace("\n", "");
        }
        PDFTextStripper pdfTextStripper = new PDFTextStripper();
        pdfTextStripper.setStartPage(1);
        pdfTextStripper.setEndPage(1);
        String pageText = pdfTextStripper.getText(pdDocument);
        return pageText.replace("\n", " ");
    }
}
