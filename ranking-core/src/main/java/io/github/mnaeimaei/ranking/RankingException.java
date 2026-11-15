package io.github.mnaeimaei.ranking;

public abstract class RankingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected RankingException(String message) {
        super(message);
    }
}
