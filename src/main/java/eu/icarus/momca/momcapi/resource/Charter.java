package eu.icarus.momca.momcapi.resource;


import eu.icarus.momca.momcapi.exception.MomcaException;
import eu.icarus.momca.momcapi.query.XpathQuery;
import eu.icarus.momca.momcapi.xml.Namespace;
import eu.icarus.momca.momcapi.xml.XmlValidationProblem;
import eu.icarus.momca.momcapi.xml.atom.AtomAuthor;
import eu.icarus.momca.momcapi.xml.atom.AtomIdCharter;
import eu.icarus.momca.momcapi.xml.cei.*;
import nu.xom.*;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a charter in MOM-CA.
 *
 * @author Daniel Jeller
 *         Created on 25.06.2015.
 */
public class Charter extends MomcaResource {

    @NotNull
    private static final String CEI_SCHEMA_URL =
            "https://raw.githubusercontent.com/icaruseu/mom-ca/master/my/XRX/src/mom/app/cei/xsd/cei10.xsd";
    @NotNull
    private final Optional<AtomAuthor> atomAuthor;
    @NotNull
    private final AtomIdCharter atomId;
    @NotNull
    private final Optional<AbstractCeiDate> ceiDate;
    @NotNull
    private final CeiIdno ceiIdno;
    @NotNull
    private final List<CeiFigure> ceiWitnessOrigFigures;
    @NotNull
    private final CharterStatus status;
    @NotNull
    private final List<XmlValidationProblem> validationProblems = new ArrayList<>(0);


    private class SimpleErrorHandler implements ErrorHandler {

        public void error(@NotNull SAXParseException e) throws SAXException {
            addToXmlValidationProblem(XmlValidationProblem.SeverityLevel.ERROR, e);
        }

        public void fatalError(@NotNull SAXParseException e) throws SAXException {
            addToXmlValidationProblem(XmlValidationProblem.SeverityLevel.FATAL_ERROR, e);
        }

        public void warning(@NotNull SAXParseException e) throws SAXException {
            addToXmlValidationProblem(XmlValidationProblem.SeverityLevel.WARNING, e);
        }

        private void addToXmlValidationProblem(@NotNull XmlValidationProblem.SeverityLevel severityLevel, @NotNull SAXParseException e) {
            validationProblems.add(new XmlValidationProblem(severityLevel, e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
        }

    }

    /**
     * Instantiates a new Charter.
     *
     * @param momcaResource The eXist-resource that the charter is based on. The XML-content is validated against the <a href="https://github.com/icaruseu/mom-ca/blob/master/my/XRX/src/mom/app/cei/xsd/cei10.xsd">CEI Schema</a>.
     */
    public Charter(@NotNull MomcaResource momcaResource) {

        super(momcaResource);

        try {
            validateCei(momcaResource);
        } catch (@NotNull SAXException | IOException | ParsingException | ParserConfigurationException e) {
            throw new IllegalArgumentException("Failed to validate the resource.", e);
        }

        status = initStatus();

        atomId = initCharterAtomId();
        atomAuthor = initAtomAuthor();
        ceiIdno = initCeiIdno();
        ceiDate = initCeiDate();
        ceiWitnessOrigFigures = new ArrayList<>(initCeiWitnessOrigFigures());

    }

    /**
     * @return The AtomAuthor.
     */
    @NotNull
    public Optional<AtomAuthor> getAtomAuthor() {
        return atomAuthor;
    }

    /**
     * @return The AtomId.
     */
    @NotNull
    public AtomIdCharter getAtomId() {
        return atomId;
    }

    /**
     * @return The date.
     */
    @NotNull
    public Optional<AbstractCeiDate> getCeiDate() {
        return ceiDate;
    }

    /**
     * @return The CeiIdno.
     */
    @NotNull
    public CeiIdno getCeiIdno() {
        return ceiIdno;
    }

    /**
     * @return A list of all figures in {@code cei:witnessOrig}. They represent only the direct images of the charter.
     */
    @NotNull
    public List<CeiFigure> getCeiWitnessOrigFigures() {
        return ceiWitnessOrigFigures;
    }

    /**
     * @return The charter's internal status (published, saved, etc.).
     */
    @NotNull
    public CharterStatus getStatus() {
        return status;
    }

    /**
     * @return A list of all validation problems.
     */
    @NotNull
    public List<XmlValidationProblem> getValidationProblems() {
        return validationProblems;
    }

    /**
     * @return {@code True} if there are any validation problems.
     * @see #getValidationProblems
     */
    public boolean isValidCei() {
        return validationProblems.isEmpty();
    }

    @NotNull
    @Override
    public String toString() {

        return "Charter{" +
                "atomAuthor=" + atomAuthor +
                ", atomId=" + atomId +
                ", ceiIdno=" + ceiIdno +
                ", ceiWitnessOrigFigures=" + ceiWitnessOrigFigures +
                ", status=" + status +
                "} " + super.toString();

    }

    private Optional<AtomAuthor> initAtomAuthor() {

        Optional<AtomAuthor> atomAuthor = Optional.empty();
        String authorEmail = queryUniqueElement(XpathQuery.QUERY_ATOM_EMAIL);
        if (!authorEmail.isEmpty()) {
            atomAuthor = Optional.of(new AtomAuthor(authorEmail));
        }
        return atomAuthor;

    }

    @NotNull
    private Optional<AbstractCeiDate> initCeiDate() {

        Optional<AbstractCeiDate> ceiDateOptional = Optional.empty();
        Nodes ceiIssuedNodes = queryContentAsNodes(XpathQuery.QUERY_CEI_ISSUED);

        if (ceiIssuedNodes.size() != 0) {

            Element ceiIssued = (Element) ceiIssuedNodes.get(0);
            Elements dateElements = ceiIssued.getChildElements("date", Namespace.CEI.getUri());
            Elements dateRangeElements = ceiIssued.getChildElements("dateRange", Namespace.CEI.getUri());

            if (dateElements.size() == 1 && dateRangeElements.size() == 0) {

                Element dateElement = dateElements.get(0);
                String value = dateElement.getAttributeValue("value");
                String literalDate = dateElement.getValue();
                ceiDateOptional = Optional.of(new CeiDate(value, literalDate));

            } else if (dateElements.size() == 0 && dateRangeElements.size() == 1) {

                Element dateRangeElement = dateRangeElements.get(0);
                String from = dateRangeElement.getAttributeValue("from");
                String to = dateRangeElement.getAttributeValue("to");
                String literalDate = dateRangeElement.getValue();
                ceiDateOptional = Optional.of(new CeiDateRange(from, to, literalDate));

            } else if (dateElements.size() == 1 && dateRangeElements.size() == 1) {

                throw new MomcaException("Both 'cei:date' and 'cei:dateRange' present in charter XML content.");

            }

        }

        return ceiDateOptional;

    }

    @NotNull
    private CeiIdno initCeiIdno() {
        String id = queryUniqueElement(XpathQuery.QUERY_CEI_BODY_IDNO_ID);
        String text = queryUniqueElement(XpathQuery.QUERY_CEI_BODY_IDNO_TEXT);
        return new CeiIdno(id, text);
    }

    @NotNull
    private List<CeiFigure> initCeiWitnessOrigFigures() {

        List<CeiFigure> ceiFigures = new ArrayList<>(0);
        Nodes figureNodes = queryContentAsNodes(XpathQuery.QUERY_CEI_WITNESS_ORIG_FIGURE);

        for (int i = 0; i < figureNodes.size(); i++) {

            Element figureElement = (Element) figureNodes.get(i);
            String nAttribute = figureElement.getAttribute("n") == null ? "" : figureElement.getAttribute("n").getValue();
            Elements childElements = figureElement.getChildElements("graphic", Namespace.CEI.getUri());

            switch (childElements.size()) {

                case 0:
                    break;

                case 1:
                    Element graphicElement = childElements.get(0);
                    String urlAttribute = (graphicElement.getAttribute("url") == null)
                            ? "" : childElements.get(0).getAttribute("url").getValue();
                    String textContent = graphicElement.getValue();
                    ceiFigures.add(new CeiFigure(urlAttribute, nAttribute, textContent));
                    break;

                default:
                    throw new IllegalArgumentException(
                            "More than one child-elements of 'cei:figure'. Only one allowed, 'cei:graphic'.");

            }

        }

        return ceiFigures;

    }

    @NotNull
    private AtomIdCharter initCharterAtomId() {

        String idString = queryUniqueElement(XpathQuery.QUERY_ATOM_ID);

        if (idString.isEmpty()) {
            String errorMessage = String.format("No atom:id in xml content: '%s'", getXmlAsDocument().toXML());
            throw new IllegalArgumentException(errorMessage);
        } else {
            return new AtomIdCharter(idString);
        }

    }

    private CharterStatus initStatus() {

        CharterStatus status;

        if (getParentUri().contains(ResourceRoot.METADATA_CHARTER_IMPORT.getCollectionName())) {
            status = CharterStatus.IMPORTED;
        } else if (getParentUri().contains(ResourceRoot.XRX_USER.getCollectionName())) {
            status = CharterStatus.PRIVATE;
        } else if (getParentUri().contains(ResourceRoot.METADATA_CHARTER_SAVED.getCollectionName())) {
            status = CharterStatus.SAVED;
        } else {
            status = CharterStatus.PUBLIC;
        }

        return status;

    }

    @NotNull
    private String queryUniqueElement(@NotNull XpathQuery query) {

        List<String> atomQueryResults = queryContentAsList(query);

        String result;

        switch (atomQueryResults.size()) {

            case 0:
                result = "";
                break;

            case 1:
                result = atomQueryResults.get(0);
                break;

            default:
                String errorMessage = String.format("More than one results for Query '%s'", query.asString());
                throw new IllegalArgumentException(errorMessage);

        }

        return result;

    }

    private void validateCei(@NotNull MomcaResource resource)
            throws SAXException, ParserConfigurationException, ParsingException, IOException {

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        factory.setSchema(schemaFactory.newSchema(new Source[]{new StreamSource(CEI_SCHEMA_URL)}));

        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(new SimpleErrorHandler());

        Nodes ceiTextNodes = resource.queryContentAsNodes(XpathQuery.QUERY_CEI_TEXT);

        if (ceiTextNodes.size() != 1) {
            throw new IllegalArgumentException("XML content has no 'cei:text' element therefor it is probably not" +
                    " a mom-ca charter.");
        }

        Element ceiTextElement = (Element) ceiTextNodes.get(0);

        Builder builder = new Builder(reader);
        builder.build(ceiTextElement.toXML(), Namespace.CEI.getUri());

    }

}
