package eu.icarus.momca.momcapi.exception;

import org.jetbrains.annotations.NotNull;

/**
 * Created by daniel on 25.06.2015.
 */
public class MomCAException extends Exception {

    public MomCAException() {
        super();
    }

    public MomCAException(@NotNull String message) {
        super(message);
    }

    public MomCAException(@NotNull String message, @NotNull Exception e) {
        super(message, e);
    }

}
