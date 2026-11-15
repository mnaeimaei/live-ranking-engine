package io.github.mnaeimaei.ranking;

/** Thrown when adding a key that is already live. */
public final class EntryAlreadyExistsException extends RankingException {

    private static final long serialVersionUID = 1L;

    private final transient Object key;

    /**
     * @param key the offending key
     */
    public EntryAlreadyExistsException(Object key) {
        super("An entry already exists for key: " + key);
        this.key = key;
    }

    /**
     * @return the offending key
     */
    public Object key() {
        return key;
    }
}
