package eu.icarus.momca.momcapi;

import eu.icarus.momca.momcapi.exist.ExistQueryFactory;
import eu.icarus.momca.momcapi.exist.MetadataCollectionName;
import eu.icarus.momca.momcapi.resource.Charter;
import eu.icarus.momca.momcapi.resource.User;
import eu.icarus.momca.momcapi.resource.atom.AtomIdCharter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by daniel on 03.07.2015.
 */
public class CharterManager {

    @NotNull
    private static final ExistQueryFactory QUERY_FACTORY = new ExistQueryFactory();

    private final MomcaConnection momcaConnection;

    CharterManager(MomcaConnection momcaConnection) {
        this.momcaConnection = momcaConnection;
    }

    @NotNull
    public List<Charter> getCharterInstances(@NotNull AtomIdCharter atomIdCharter) {

        return momcaConnection.queryDatabase(QUERY_FACTORY.queryCharterUris(atomIdCharter)).stream()
                .map(this::getCharterFromUri)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

    }

    @NotNull
    public List<AtomIdCharter> listErroneouslySavedCharters(@NotNull User user) {
        return user.listSavedCharterIds().stream()
                .filter(charterAtomId -> !isCharterExisting(charterAtomId, MetadataCollectionName.METADATA_CHARTER_SAVED))
                .collect(Collectors.toList());
    }

    @NotNull
    private Optional<Charter> getCharterFromUri(@NotNull String charterUri) {
        String resourceName = charterUri.substring(charterUri.lastIndexOf('/') + 1, charterUri.length());
        String parentUri = charterUri.substring(0, charterUri.lastIndexOf('/'));
        return momcaConnection.getExistResource(resourceName, parentUri).map(Charter::new);
    }

    private boolean isCharterExisting(@NotNull AtomIdCharter atomIdCharter, @Nullable MetadataCollectionName metadataCollectionName) {
        return !momcaConnection.queryDatabase(QUERY_FACTORY.queryCharterExistence(atomIdCharter, metadataCollectionName)).isEmpty();
    }

}
