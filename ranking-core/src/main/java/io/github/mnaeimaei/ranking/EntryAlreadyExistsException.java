package io.github.mnaeimaei.ranking;

public final class EntryAlreadyExistsException extends RankingException {

    private static final long serialVersionUID = 1L;

    private final transient Object key;

    public EntryAlreadyExistsException(Object key) {
        super("An entry already exists for key: " + key);
        this.key = key;
    }

    public Object key() {
        return key;
    }
}
