package io.github.mnaeimaei.ranking;

/** Base type for every exception thrown by the ranking engine. */
public abstract class RankingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message detail message
     */
    protected RankingException(String message) {
        super(message);
    }
}
