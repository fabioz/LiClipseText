package org.brainwy.liclipsetext.shared_core.document;

public class DocumentTimeStampChangedException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @FunctionalInterface
    public interface Supplier<T> {

        /**
         * Gets a result.
         *
         * @return a result
         */
        T get() throws DocumentTimeStampChangedException;
    }

    public static <T> T retryUntilNoDocChanges(Supplier<T> supplier) {
        while (true) {
            try {
                return supplier.get();
            } catch (DocumentTimeStampChangedException e) {
                // Ignore and reprocess.
            }
        }
    }
}
