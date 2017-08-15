
public class Duration {
	
	static final String ZERO_TIME = "00:00:00";
	
	static int HHMMSS_to_sec(String hhmmss) {

		int sec = 0;
		
		sec += Integer.parseInt(hhmmss.substring(0, 2)) * 3600;
		
		sec += Integer.parseInt(hhmmss.substring(3, 5)) * 60;
		
		sec += Integer.parseInt(hhmmss.substring(6, 8));
		
		return sec;
	}
	
	static String sec_to_HHMMSS(int sec) {
		
		String hhmmss;
		StringBuffer sbuff = new StringBuffer(8);
		
		int hours = sec / 3600;		
		if (hours > 9) {
			sbuff.append(hours);
		} else {
			sbuff.append(0);
			sbuff.append(hours);
		}
		sec = sec - hours * 3600;
		sbuff.append(':');
		
		int mins = sec / 60;		
		if (mins > 9) {
			sbuff.append(mins);
		} else {
			sbuff.append(0);
			sbuff.append(mins);
		}
		sec = sec - mins * 60;
		sbuff.append(':');
		
		sbuff.append(sec);
		hhmmss = sbuff.toString();
		
		return hhmmss;
	}
	
}
