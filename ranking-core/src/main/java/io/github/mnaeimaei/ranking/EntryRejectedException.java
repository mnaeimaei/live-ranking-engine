package io.github.mnaeimaei.ranking;

public class EntryRejectedException extends RankingException {

    private static final long serialVersionUID = 1L;

    public EntryRejectedException(String reason) {
        super(reason);
    }
}
