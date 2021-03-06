package edu.nju.http.message.parser.transdecode;

import edu.nju.http.exception.InvalidMessageException;
import edu.nju.http.message.parser.CustomizedReader;

import java.util.Map;

public abstract class TransDecodeStrategy {
    protected
    CustomizedReader reader;

    // ==================== Methods ==================== //

    // -------------------- public -------------------- //

    public void init(CustomizedReader reader) {
        this.reader = reader;
    }

    public void init(byte[] array) {
        this.reader = new CustomizedReader(array);
    }

    public abstract byte[] getBody(Map<String, String> headers) throws InvalidMessageException;

    // -------------------- protected -------------------- //

}
