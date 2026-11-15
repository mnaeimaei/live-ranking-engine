package io.github.mnaeimaei.ranking;

/** Thrown when an {@link AdmissionRule} vetoes an entry. Domains may subclass it for specific reasons. */
public class EntryRejectedException extends RankingException {

    private static final long serialVersionUID = 1L;

    /**
     * @param reason human-readable reason for the rejection
     */
    public EntryRejectedException(String reason) {
        super(reason);
    }
}
