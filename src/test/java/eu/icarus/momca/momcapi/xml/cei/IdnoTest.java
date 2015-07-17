package eu.icarus.momca.momcapi.xml.cei;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by daniel on 09.07.2015.
 */
public class IdnoTest {

    private static final Idno CEI_IDNO = new Idno("id", "text");

    @Test
    public void testGetId() throws Exception {
        assertEquals(CEI_IDNO.getId(), "id");
    }

    @Test
    public void testGetText() throws Exception {
        assertEquals(CEI_IDNO.getText(), "text");
    }

    @Test
    public void testToXML() throws Exception {
        assertEquals(CEI_IDNO.toXML(), "<cei:idno xmlns:cei=\"http://www.monasterium.net/NS/cei\" id=\"id\">text</cei:idno>");
    }

}