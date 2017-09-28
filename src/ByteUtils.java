
public class ByteUtils {
	
	/* Input: bArr- an integer as byte array, in little-endian format || Output: unmodified integer as 'int' value  */
	static int toIntFromLittleEndian(byte[] bArr) {
		int val = 0;
		int tmp = 0;
		
		for (	int i = 0, mult = 0;
				i < bArr.length;
				i++, mult += 8) 
		{
			tmp = (bArr[i] & 0xFF);
			tmp <<= mult;
			
			val |= tmp;
		}
		
		return val;
	}
	
	/* Input: bArr- an integer as byte array, in little-endian format || Output: unmodified integer as 'long' value  */
	static long toLongFromLittleEndian(byte[] bArr) {
		long val = 0;
		long tmp = 0;
		
		for (	int i = 0, mult = 0;
				i < bArr.length;
				i++, mult += 8) 
		{
			tmp = (bArr[i] & 0xFF);
			tmp <<= mult;
			
			val |= tmp;
		}
		
		return val;
	}
	
	//side note: parameter "len" is for when padding is needed
	/* Input: arg- an integer as 'int' value, len- on how many bytes value should be written || Output: unmodified integer as byte array, in little-endian format  */
	static byte[] toLittleEndianByteArray(int arg, int len) {
		byte[] bArr = new byte[len];
		
		for (	int i = 0, mult = 0;
				i < bArr.length;
				i++, mult += 8) 
		{
			bArr[i] = (byte)(arg >> mult);
		}
		
		return bArr;		
	}

	//side note: parameter "len" is for when padding is needed
	/* Input: arg- an integer as 'long' value, len- on how many bytes value should be written || Output: unmodified integer as byte array, in little-endian format  */
	public static byte[] toLittleEndianByteArray(long arg, int len) {
	byte[] bArr = new byte[len];
		
		for (	int i = 0, mult = 0;
				i < bArr.length;
				i++, mult += 8) 
		{
			bArr[i] = (byte)(arg >> mult);
		}
		
		return bArr;	
	}	
}
