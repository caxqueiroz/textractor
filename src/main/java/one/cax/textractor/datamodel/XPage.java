package one.cax.textractor.datamodel;

import lombok.*;
import one.cax.textractor.utilities.NameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single page of a document.
 * This class encapsulates the content and metadata of a page, including its text,
 * page number, and vector representation (if available).
 *
 * The XPage class uses Lombok annotations for automatic generation of
 * getters, setters, no-args constructor, toString, equals, and hashCode methods.
 *
 * Key components:
 * - pageNumber: The number of the page within the document
 * - text: The textual content of the page
 * - vector: A float array representing the vector embedding of the page content (optional)
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class XPage {

    private int pageNumber;

    private String text;

    private float[] vector;

    /**
     * @param pagJsonObject - JSON object of a page
     * @return XPage object
     * @throws JSONException if there is an error during JSON conversion
     */
    public static XPage fromJSON(JSONObject pagJsonObject) throws JSONException {
        var xPage = new XPage();
        try {
            xPage.setPageNumber(pagJsonObject.getInt(NameUtils.PAGE_NUMBER));
            xPage.setText(pagJsonObject.getString(NameUtils.PAGE_TEXT));
            if (pagJsonObject.has(NameUtils.PAGE_VECTOR)) {
                var vectorArray = pagJsonObject.getJSONArray(NameUtils.PAGE_VECTOR);
                float[] vector = new float[vectorArray.length()];
                for (int i = 0; i < vectorArray.length(); i++) {
                    vector[i] = (float) vectorArray.getDouble(i);
                }
                xPage.setVector(vector);
            }

        } catch (JSONException e) {
            throw new JSONException("Error parsing JSON string: " + e.getMessage());
        }
        return xPage;
    }

    /**
     * Converts a JSON array to a list of XPages.
     *
     * @param pagJsonArray - JSON array of pages
     * @return list of XPages
     * @throws JSONException
     */
    public static List<XPage> fromJSONArray(JSONArray pagJsonArray) throws JSONException {
        var xPages = new ArrayList<XPage>();
        for (int i = 0; i < pagJsonArray.length(); i++) {
            xPages.add(XPage.fromJSON(pagJsonArray.getJSONObject(i)));
        }
        return xPages;
    }

    /**
     * Converts the XPage object to a JSONObject.
     *
     * @return JSONObject representation of the XPage object
     * @throws JSONException if there is an error during JSON conversion
     */
    public JSONObject toJSON() throws JSONException {
        try {
            var json = new JSONObject();
            json.put(NameUtils.PAGE_NUMBER, this.pageNumber);
            json.put(NameUtils.PAGE_TEXT, this.text);
            // Convert vector array to JSONArray
            if (this.vector != null) {
                var vectorArray = new JSONArray();
                for (float v : this.vector) {
                    vectorArray.put(v);
                }
                json.put(NameUtils.PAGE_VECTOR, vectorArray);
            }

            return json;
        } catch (JSONException e) {
            throw new JSONException("Error parsing JSON string: " + e.getMessage());
        }
    }

}
