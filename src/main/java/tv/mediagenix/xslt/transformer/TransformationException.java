package tv.mediagenix.xslt.transformer;

public class TransformationException extends Exception {
    public TransformationException(Exception e) {
        super(e);
    }

    public TransformationException(String msg) {
        super(msg);
    }

}