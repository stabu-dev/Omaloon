package ol.world.blocks.crafting.multicraft;

public class RecipeAnalyzerException extends RuntimeException{
    public RecipeAnalyzerException() {
        super();
    }

    public RecipeAnalyzerException(String message) {
        super(message);
    }

    public RecipeAnalyzerException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecipeAnalyzerException(Throwable cause) {
        super(cause);
    }

    protected RecipeAnalyzerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
