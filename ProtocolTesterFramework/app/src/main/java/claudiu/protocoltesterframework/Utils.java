package claudiu.protocoltesterframework;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Bogdan on 8/7/2015.
 */
public class Utils {

	public static int bytesToInt(byte[] value) {

		int result;

		ByteBuffer bb = ByteBuffer.wrap(value);
		bb.order(ByteOrder.BIG_ENDIAN);

		result = bb.getInt();

		return result;
	}

	public static byte[] intToBytes(int value) {

		byte[] result;

		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putInt(value);

		result = bb.array();

		return result;
	}
}
