import java.io.File;

public class Main {

	public static void main(String[] args) {
		//simply run GUI when no args
		//only SPLIT MODE is available in GUI
		if (args.length == 0) {
			new GUI_Frame();
		} else {
			//by default SPLI MODE is engaged as follows:
			//arg1: file path
			//arg2: 'from' in HH:MM:SS
			//arg3: 'to' in HH:MM:SS
			if ( ! args[0].equals("-join")) {
				
				new WAVFile(new File(args[0])).cutFromTo(Duration.HHMMSS_to_sec(args[1]), Duration.HHMMSS_to_sec(args[2]));
				
				//JOIN MODE where files are joined via their order of passing them as next args
			} else {
				//result file will have properties of first passed file
				WAVFile[] WAVfiles = new WAVFile[args.length - 1];
				
				for (int i = 0; i < WAVfiles.length; i++) {
					WAVfiles[i] = new WAVFile(new File(args[i + 1]));
				}
				
				WAVFile.join(WAVfiles);
			}
		}

	}
	
}
