import java.io.File;

public class Main {

	public static void main(String[] args) {
		
		// run GUI when no args
		// only SPLIT MODE is available in GUI
		if (args.length == 0) {
			new GUIFrame();
			return;
		}
		
		// SPLIT MODE is engaged as follows:
		// arg1: file path
		// arg2: 'from' in HH:MM:SS
		// arg3: 'to' in HH:MM:SS
		if (args[0].equals("-split")) {
			new WAVFile(new File(args[1])).cutFromTo(Duration.HHMMSSToSec(args[2]), Duration.HHMMSSToSec(args[3]));
			System.out.println("AudioManip- splitting succesful");
			return;
		}
				
		// JOIN MODE where files are joined via their order of passing them as next args
		// result file will have properties of first passed file
		if (args[0].equals("-join")) {
			WAVFile[] WAVfiles = new WAVFile[args.length - 1];
			
			for (int i = 0; i < WAVfiles.length; i++) {
				WAVfiles[i] = new WAVFile(new File(args[i + 1]));
			}
			
			WAVFile.join(WAVfiles);
			System.out.println("AudioManip- joining succesful");
			return;
		}
		
		// INFO MODE which prints info about passed files
		if (args[0].equals("-info")) {
			WAVFile current = null;
			
			for (int i = 1; i < args.length; i++) {				
				current = new WAVFile(new File(args[i]));
				current.stdPrintInfo();
			}
			
			return;
		}
		
		// BYTE SPLIT MODE acts as normal SPLIT MODE, but instead of specifying time, we operate by numbers of bytes, it allows for much greater precision when needed
		// use INFO MODE to find out how many bytes you need for one second of audio(bytes per second)
		// navigate to start fragment of audio clip which you want to split and change HH:MM:SS to seconds, then multiply it by bytes per second which you got from INFO MODE
		// do the same thing with the end of fragment as you normally would do in SPLIT MODE
		// pass results to program like: AudioManip.jar -bytesplit <file> <start number of bytes> <end number of bytes>
		if (args[0].equals("-bytesplit")) {
			new WAVFile(new File(args[1])).byteCutFromTo(Long.parseLong(args[2]),Long.parseLong(args[3]));
			System.out.println("AudioManip- byte splitting succesful");
			return;
		}
	}
}
