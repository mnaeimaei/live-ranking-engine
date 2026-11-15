package io.github.mnaeimaei.ranking;

/** Thrown when an operation targets a key that is not live. */
public final class EntryNotFoundException extends RankingException {

    private static final long serialVersionUID = 1L;

    private final transient Object key;

    /**
     * @param key the offending key
     */
    public EntryNotFoundException(Object key) {
        super("No entry found for key: " + key);
        this.key = key;
    }

    /**
     * @return the offending key
     */
    public Object key() {
        return key;
    }
}
