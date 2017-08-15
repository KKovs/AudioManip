
public class ByteUtils {
	
	static int toInt_fromLittleEndian(byte[] bArr) {
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
}
