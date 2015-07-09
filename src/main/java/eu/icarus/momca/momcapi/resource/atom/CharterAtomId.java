package eu.icarus.momca.momcapi.resource.atom;

import eu.icarus.momca.momcapi.Util;
import eu.icarus.momca.momcapi.resource.ResourceType;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

/**
 * Created by daniel on 25.06.2015.
 */
public class CharterAtomId extends AtomId {

    @NotNull
    private final Optional<String> archiveId;
    @NotNull
    private final String charterId;
    @NotNull
    private final Optional<String> collectionId;
    @NotNull
    private final Optional<String> fondId;

    public CharterAtomId(@NotNull String value) {

        super(value);
        String[] valueTokens = value.split("/");

        if (!valueTokens[1].equals(ResourceType.CHARTER.getValue())) {
            throw new IllegalArgumentException(String.format("'%s' identifies a '%s', not a charter.", value, valueTokens[1]));
        }

        try {

            switch (valueTokens.length) {

                case 4:
                    this.collectionId = Optional.of(Util.decode(valueTokens[2]));
                    this.charterId = Util.decode(valueTokens[3]);
                    this.archiveId = Optional.empty();
                    this.fondId = Optional.empty();
                    break;
                case 5:
                    this.archiveId = Optional.of(Util.decode(valueTokens[2]));
                    this.fondId = Optional.of(Util.decode(valueTokens[3]));
                    this.charterId = Util.decode(valueTokens[4]);
                    this.collectionId = Optional.empty();
                    break;
                default:
                    throw new IllegalArgumentException(String.format("'%s' is not a valid charterId.", value));

            }

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    public CharterAtomId(@NotNull String archiveId, @NotNull String fondId, @NotNull String charterId) {

        super(ResourceType.CHARTER.getValue(), archiveId, fondId, charterId);
        this.archiveId = Optional.of(archiveId);
        this.fondId = Optional.of(fondId);
        this.charterId = charterId;
        this.collectionId = Optional.empty();

    }

    public CharterAtomId(@NotNull String collectionId, @NotNull String charterId) {

        super(ResourceType.CHARTER.getValue(), collectionId, charterId);
        this.collectionId = Optional.of(collectionId);
        this.charterId = charterId;
        this.archiveId = Optional.empty();
        this.fondId = Optional.empty();

    }

    @NotNull
    public Optional<String> getArchiveId() {
        return archiveId;
    }

    @NotNull
    public String getBasePath() {
        return isPartOfArchiveFond() ? getArchiveId().get() + "/" + getFondId().get() : getCollectionId().get();
    }

    @NotNull
    public String getCharterId() {
        return charterId;
    }

    @NotNull
    public Optional<String> getCollectionId() {
        return collectionId;
    }

    @NotNull
    public Optional<String> getFondId() {
        return fondId;
    }

    public boolean isPartOfArchiveFond() {
        return (archiveId.isPresent() && fondId.isPresent()) && !collectionId.isPresent();
    }

    public boolean isPartOfCollection() {
        return (!archiveId.isPresent() && !fondId.isPresent()) && collectionId.isPresent();
    }

    @NotNull
    @Override
    public String toString() {

        return "CharterAtomId{" +
                "collectionId=" + collectionId +
                ", archiveId=" + archiveId +
                ", fondId=" + fondId +
                ", charterId='" + charterId + '\'' +
                '}';

    }

}