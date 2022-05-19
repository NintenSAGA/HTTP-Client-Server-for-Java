package util.parser.transdecode;

import exception.InvalidMessageException;
import util.parser.CustomerReader;

import java.util.Map;

public abstract class TransDecodeStrategy {
    protected
    CustomerReader reader;

    // ==================== Methods ==================== //

    // -------------------- public -------------------- //

    public void init(CustomerReader reader) {
        this.reader = reader;
    }

    public void init(byte[] array) {
        this.reader = new CustomerReader(array);
    }

    public abstract byte[] getBody(Map<String, String> headers) throws InvalidMessageException;

    // -------------------- protected -------------------- //

}
