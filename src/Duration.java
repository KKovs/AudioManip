
public class Duration {
	
	static final String ZERO_TIME = "00:00:00";
	
	/* Input: hhmmss- hhmmss in string || Output: returns hhmmss in seconds */
	static int HHMMSSToSec(String hhmmss) {

		int sec = 0;
		
		sec += Integer.parseInt(hhmmss.substring(0, 2)) * 3600;
		
		sec += Integer.parseInt(hhmmss.substring(3, 5)) * 60;
		
		sec += Integer.parseInt(hhmmss.substring(6, 8));
		
		return sec;
	}
	
	/* Input: hhmmss- hhmmss in seconds || Output: returns hhmmss in string */
	static String secToHHMMSS(int sec) {
		
		String hhmmss;
		StringBuffer buffer = new StringBuffer(8);
		
		int hours = sec / 3600;		
		if (hours > 9) {
			buffer.append(hours);
		} else {
			buffer.append(0);
			buffer.append(hours);
		}
		sec = sec - hours * 3600;
		buffer.append(':');
		
		int mins = sec / 60;		
		if (mins > 9) {
			buffer.append(mins);
		} else {
			buffer.append(0);
			buffer.append(mins);
		}
		sec = sec - mins * 60;
		buffer.append(':');
		
		if (sec > 9) {
			buffer.append(sec);
		} else {
			buffer.append(0);
			buffer.append(sec);
		}

		hhmmss = buffer.toString();
		
		return hhmmss;
	}
	
}
