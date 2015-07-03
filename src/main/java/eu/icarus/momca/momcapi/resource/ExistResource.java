package eu.icarus.momca.momcapi.resource;


import eu.icarus.momca.momcapi.Util;
import nu.xom.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by daniel on 24.06.2015.
 */
public class ExistResource {

    @NotNull
    private final String parentUri;
    @NotNull
    private final String resourceName;
    @NotNull
    private final Document xmlAsDocument;

    ExistResource(@NotNull final ExistResource existResource) {
        this.resourceName = existResource.getResourceName();
        this.xmlAsDocument = existResource.getXmlAsDocument();
        this.parentUri = existResource.getParentUri();
    }

    public ExistResource(@NotNull final String resourceName, @NotNull final String parentCollectionUri, @NotNull final String xmlContent) throws ParsingException, IOException {
        this.resourceName = Util.encode(resourceName);
        this.xmlAsDocument = parseXmlString(xmlContent);
        this.parentUri = Util.encode(parentCollectionUri);
    }

    @NotNull
    final List<String> queryContentXml(@NotNull XpathQuery query) {

        List<String> results = new LinkedList<>();

        Element root = getXmlAsDocument().getRootElement();
        XPathContext context = XPathContext.makeNamespaceContext(root);
        if (query.getNamespaces().length != 0) {
            for (Namespace namespace : query.getNamespaces()) {
                context.addNamespace(namespace.getPrefix(), namespace.getUri());
            }

        }

        Nodes nodes = getXmlAsDocument().getRootElement().query(query.getQuery(), context);
        for (int i = 0; i < nodes.size(); i++) {
            results.add(nodes.get(i).getValue());
        }

        return results;

    }

    @NotNull
    public String getParentUri() {
        return parentUri;
    }

    @NotNull
    public String getResourceName() {
        return resourceName;
    }

    @NotNull
    public String getUri() {
        return parentUri + "/" + resourceName;
    }

    @NotNull
    public Document getXmlAsDocument() {
        return xmlAsDocument;
    }

    @Override
    public String toString() {
        return "ExistResource{" +
                "resourceName='" + resourceName + '\'' +
                ", xmlAsDocument=" + xmlAsDocument +
                ", parentUri='" + parentUri + '\'' +
                '}';
    }

    @NotNull
    private Document parseXmlString(@NotNull String xmlAsString) throws ParsingException, IOException {
        Builder parser = new Builder();
        return parser.build(xmlAsString, null);
    }

}
