package eu.icarus.momca.momcapi;

import eu.icarus.momca.momcapi.model.xml.atom.AtomId;
import eu.icarus.momca.momcapi.query.XpathQuery;
import nu.xom.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Utility functions.
 *
 * @author Daniel Jeller
 *         Created on 03.07.2015.
 */
public class Util {

    /**
     * Decodes a string that is %-encoded, e.g. {@code user%40mail.com} gets decoded to {@code user@mail.com}.
     *
     * @param string The string to decode.
     * @return The decoded string.
     */
    @NotNull
    public static String decode(@NotNull String string) {

        List<String> decodedTokens = new ArrayList<>(0);
        for (String token : string.split("/")) {
            try {
                decodedTokens.add(URLDecoder.decode(token, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return String.join("/", decodedTokens);

    }

    /**
     * %-encodes a string, e.g. {@code user@mail.com} gets encoded to {@code user%40mail.com}.
     *
     * @param string The string to encode.
     * @return The encoded string.
     */
    @NotNull
    public static String encode(@NotNull String string) {

        List<String> encodedTokens = new ArrayList<>(0);
        for (String token : string.split("/")) {

            if (token.equals(AtomId.DEFAULT_PREFIX)) {
                encodedTokens.add(token);
            } else {
                try {
                    encodedTokens.add(URLEncoder.encode(URLDecoder.decode(token, "UTF-8"), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return String.join("/", encodedTokens);

    }

    /**
     * @param uri An URI.
     * @return The last URI part, e.g. {@code admin.xml} for {@code /db/mom-data/user.xrx/admin.xml}
     */
    @NotNull
    public static String getLastUriPart(@NotNull String uri) {
        testIfUri(uri);
        return uri.substring(uri.lastIndexOf('/') + 1, uri.length());
    }

    /**
     * @param uri An URI.
     * @return The parent of the last URI item, e.g. {@code /db/mom-data/user.xrx} for
     * {@code /db/mom-data/user.xrx/admin.xml}
     */
    @NotNull
    public static String getParentUri(@NotNull String uri) {
        testIfUri(uri);
        return uri.substring(0, uri.lastIndexOf('/'));
    }

    @NotNull
    private static XPathContext getxPathContext(@NotNull Element root, @NotNull XpathQuery query) {
        XPathContext context = XPathContext.makeNamespaceContext(root);
        query.getNamespaces().forEach(n -> context.addNamespace(n.getPrefix(), n.getUri()));
        return context;
    }

    @NotNull
    public static Document parseToDocument(@NotNull String xml) {

        Builder builder = new Builder();
        try {

            return builder.build(xml, null);

        } catch (ParsingException e) {
            throw new IllegalArgumentException("Failed to parse xml.", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @NotNull
    public static Element parseToElement(@NotNull String xml) {
        Document doc = parseToDocument(xml);
        return (Element) doc.getRootElement().copy();
    }

    /**
     * The Xpath Query to execute on the content.
     *
     * @param query the query
     * @return A list of the results as strings.
     */
    @NotNull
    public static List<String> queryXmlToList(@NotNull Element xml, @NotNull XpathQuery query) {

        Nodes nodes = queryXmlToNodes(xml, query);
        List<String> results = new LinkedList<>();

        for (int i = 0; i < nodes.size(); i++) {
            results.add(nodes.get(i).getValue());
        }

        return results;

    }

    /**
     * Query the resource's XML content.
     *
     * @param query The Xpath Query to execute on the content.
     * @return The nodes containing the results.
     */
    @NotNull
    public static Nodes queryXmlToNodes(@NotNull Element xml, @NotNull XpathQuery query) {

        String queryString = query.asString();
        XPathContext context = getxPathContext(xml, query);

        return xml.query(queryString, context);

    }

    @NotNull
    public static Optional<String> queryXmlToOptional(@NotNull Element xml, @NotNull XpathQuery query) {

        String queryResult = queryXmlToString(xml, query);
        Optional<String> result = Optional.empty();

        if (!queryResult.isEmpty()) {
            result = Optional.of(queryResult);
        }

        return result;

    }

    @NotNull
    public static String queryXmlToString(@NotNull Element xml, @NotNull XpathQuery query) {

        List<String> queryResults = queryXmlToList(xml, query);

        String result;

        switch (queryResults.size()) {

            case 0:
                result = "";
                break;

            case 1:
                result = queryResults.get(0);
                break;

            default:
                String errorMessage = String.format("More than one results for Query '%s'", query.asString());
                throw new IllegalArgumentException(errorMessage);

        }

        return result;

    }

    private static void testIfUri(@NotNull String possibleUri) {

        if (!possibleUri.contains("/")) {
            String message = String.format("'%s' is probably not a valid uri, it doesn't contain '/'.", possibleUri);
            throw new IllegalArgumentException(message);
        }

    }

}