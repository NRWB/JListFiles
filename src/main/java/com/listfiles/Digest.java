
public interface Digest {

	public static final char[] base16 = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	public static String getDigestHash(final byte[] arr) {
		final int len = arr.length;
		StringBuilder result = new StringBuilder(len * 2);
	    for (int i = 0; i < len; ++i) {
	    	final byte b = arr[i];
	    	result.append(base16[(b >> 4) & 0xF]);
	    	result.append(base16[(b & 0xF)]);
	    }
	    return result.toString();
	}

}
