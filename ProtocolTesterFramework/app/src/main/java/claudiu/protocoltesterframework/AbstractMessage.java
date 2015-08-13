package claudiu.protocoltesterframework;

/**
 * Created by Bogdan on 8/8/2015.
 */
public class AbstractMessage {

    public long id;
    public long timestamp;
}

class LargeMessage extends AbstractMessage {

    public int[] data;
}