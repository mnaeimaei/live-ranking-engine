package io.github.mnaeimaei.ranking;

public final class EntryNotFoundException extends RankingException {

    private static final long serialVersionUID = 1L;

    private final transient Object key;

    public EntryNotFoundException(Object key) {
        super("No entry found for key: " + key);
        this.key = key;
    }

    public Object key() {
        return key;
    }
}
