package eu.icarus.momca.momcapi.resource;

/**
 * Created by daniel on 27.06.2015.
 */
public enum XpathQuery {

    QUERY_ATOM_ID("//atom:id/text()", Namespace.ATOM),
    QUERY_CONFIG_GROUP_NAME("//config:group/@name", Namespace.CONFIG),
    QUERY_CONFIG_NAME("//config:name", Namespace.CONFIG),
    QUERY_NAME("//name/text()"),
    QUERY_XRX_BOOKMARK("//xrx:bookmark/text()", Namespace.XRX),
    QUERY_XRX_EMAIL("//xrx:email/text()", Namespace.XRX),
    QUERY_XRX_MODERATOR("//xrx:moderator/text()", Namespace.XRX),
    QUERY_XRX_NAME("//xrx:name/text()", Namespace.XRX),
    QUERY_XRX_SAVED("//xrx:saved/xrx:id/text()", Namespace.XRX);

    private final Namespace[] namespaces;
    private final String query;

    XpathQuery(String query, Namespace... namespaces) {
        this.query = query;
        this.namespaces = namespaces;
    }

    public Namespace[] getNamespaces() {
        return namespaces;
    }

    public String getQuery() {
        return query;
    }

}
