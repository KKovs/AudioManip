import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class WAVFile {

	static final int CANON_HEADER_SIZE = 44;

	static final int CUSTOM_BUFFER_SIZE = 1500000000;

	// --utility fields--
	private FileInputStream fInputStream;
	private FileOutputStream fOutputStream;
	private String fileName;
	private String filePath;
	// --fields contained in WAV file--
	private long fileSize;
	private int formatChunkSize;
	private int format;
	private int channelsNumber;
	private int samplerate;
	private int bitrate;
	private int blockAlign;
	private int bitsPerSample;
	private long dataSize;
	private byte[] data;
	// --fields calculated from those from file--
	private int bytesPerSec;
	private boolean hugeDataFlag;

	public WAVFile(File file) {

		try {
			// getting misc info
			filePath = file.getParent();
			fileName = file.getName();
			fInputStream = new FileInputStream(file);

			// loading file header	
			// byte offset//field name
			
			// 0//RIFF marker
			fInputStream.skip(4);

			// 4//file size
			byte[] bArr = new byte[4];

			fInputStream.read(bArr);

			fileSize = ByteUtils.toLongFromLittleEndian(bArr);

			// 8//format
			fInputStream.skip(4);

			// 12//chunk id
			fInputStream.skip(4);

			// 16//format chunk size
			bArr = new byte[4];

			fInputStream.read(bArr);

			formatChunkSize = ByteUtils.toIntFromLittleEndian(bArr);

			// 20//audio format
			bArr = new byte[2];
			fInputStream.read(bArr);

			format = ByteUtils.toIntFromLittleEndian(bArr);

			// 22//no of channels
			bArr = new byte[2];
			fInputStream.read(bArr);

			channelsNumber = ByteUtils.toIntFromLittleEndian(bArr);

			// 24//sample rate
			bArr = new byte[4];
			fInputStream.read(bArr);

			samplerate = ByteUtils.toIntFromLittleEndian(bArr);

			// 28//byte rate
			bArr = new byte[4];
			fInputStream.read(bArr);

			bitrate = ByteUtils.toIntFromLittleEndian(bArr);

			// 32//block alignment
			bArr = new byte[2];
			fInputStream.read(bArr);

			blockAlign = ByteUtils.toIntFromLittleEndian(bArr);

			// 34//bits per sample
			bArr = new byte[2];
			fInputStream.read(bArr);

			bitsPerSample = ByteUtils.toIntFromLittleEndian(bArr);

			// 36//data id
			////
			// data chunk location may vary in non-canon so we check
			////
			while (fInputStream.available() > 0)
				if (fInputStream.read() == 'd')
					if (fInputStream.read() == 'a')
						if (fInputStream.read() == 't')
							if (fInputStream.read() == 'a')
								break;

			// 40//data size
			bArr = new byte[4];
			fInputStream.read(bArr);

			// to long since dataSize may actually cover all 4 bytes
			dataSize = ByteUtils.toLongFromLittleEndian(bArr);

			// 44//END OF HEADER, now only sound data is left
			
			// calculating bps
			bytesPerSec = (bitsPerSample * samplerate * channelsNumber) / 8;
			
			// we are aiming at loading whole data into memory, so the process will be much faster
			// however when it is not possible(sound data is big or memory is already occupied) we will load sound data later
			// and through custom buffer
			
			// if dataSize > Integer.MAX_VALUE creating array with dataSize length is not possible
			if (dataSize <= Integer.MAX_VALUE) {
				
				try {
					// but if dataSize <= Integer.MAX_VALUE we can at least try...
					data = new byte[(int) dataSize];
				} catch (OutOfMemoryError e) {
					// ...when something goes wrong we initiate loading through buffer
					hugeDataFlag = true;
					return;
				}
				// this line executes when whole sound data can be loaded into memory
				fInputStream.read(data);

			} else {
				// dataSize > Integer.MAX_VALUE -> sound data will be later loaded through buffer
				hugeDataFlag = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// end of constructor
	}

	
	/* Input: from- starting point in seconds, to- end point in seconds || Output: returns nothing, starting point of splitting WAV file procedure */
	public void cutFromTo(int from, int to) {
		
		// calculating ranges in bytes basing on bps
		long from_bytes = (long)bytesPerSec * from;
		long to_bytes = (long)bytesPerSec * to;
		long diff = to_bytes - from_bytes;
	
		// when sound data couldnt have been loaded in constructor
		if (hugeDataFlag) {
			dataSize = diff;
			// firstly header
			write_only_header();
			// then 'huge' data
			write_only_huge_data(this.fOutputStream, this.fInputStream, from_bytes, diff);
			
			// thats all
			try {
				fInputStream.close();
				fOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		// sound data was loaded in constructor
		// can we allocate another array, big enough to contain newly selected data
		try {
			byte[] tempbuffer = new byte[(int) diff];
			for (int i = (int) from_bytes; i < to_bytes; i++) {
				tempbuffer[i - ((int) from_bytes)] = data[i];
			}

			data = tempbuffer;

		} catch (OutOfMemoryError e) {
			// if OutOfMemory error occurs here it means in constructor there was no
			// OOM error (hugeDataFlag) so two huge arrays are too big
			//
			// in this situation we allocate new FileInputStream and we read just sound data from it
			// substituting previous data array
			data = null;
			data = new byte[(int) diff];
			try {
				fInputStream = null;
				fInputStream = new FileInputStream(new File(fileName));
				fInputStream.skip(CANON_HEADER_SIZE);
				fInputStream.skip(from_bytes);

				fInputStream.read(data);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
		
		dataSize = data.length;
		// all fields are ready to be written
		write_all(true);
	}
	
	/* Input: from_bytes- starting point in bytes, to_bytes- end point in bytes || Output: returns nothing, starting point of splitting WAV file procedure */
	public void byteCutFromTo(long from_bytes, long to_bytes) {
		long diff = to_bytes - from_bytes;
		
		// when sound data couldnt have been loaded in constructor
		if (hugeDataFlag) {
			dataSize = diff;
			// firstly header
			write_only_header();
			// then 'huge' data
			write_only_huge_data(this.fOutputStream, this.fInputStream, from_bytes, diff);
			
			// thats all
			try {
				fInputStream.close();
				fOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		// sound data was loaded in constructor
		// can we allocate another array, big enough to contain newly selected data
		try {
			byte[] tempbuffer = new byte[(int) diff];
			for (int i = (int) from_bytes; i < to_bytes; i++) {
				tempbuffer[i - ((int) from_bytes)] = data[i];
			}

			data = tempbuffer;

		} catch (OutOfMemoryError e) {
			// if OutOfMemory error occurs here it means in constructor there was no
			// OOM error (hugeDataFlag) so two huge arrays are too big
			//
			// in this situation we allocate new FileInputStream and we read just sound data from it
			// substituting previous data array
			data = null;
			data = new byte[(int) diff];
			try {
				fInputStream = null;
				fInputStream = new FileInputStream(new File(fileName));
				fInputStream.skip(CANON_HEADER_SIZE);
				fInputStream.skip(from_bytes);

				fInputStream.read(data);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
		
		dataSize = data.length;
		// all fields are ready to be written
		write_all(true);
	}

	/* Input: array of WAV files to be joined || Output: returns nothing, starting point of joining WAV file procedure */
	public static void join(WAVFile[] files) {

		// first file as template for WAV file properties
		WAVFile result = files[0];
		//lets start by writing all what files[0] has to write and keep its output stream open
		
		// temp is for storing dataSize since we will need it unchanged in a loop
		long temp = result.dataSize;
		for (int i = 1; i < files.length; i++) {
			result.dataSize += files[i].dataSize;
		}
		result.write_only_header();
		//append only data of files
		try {
			// depending if file is huge or not, use appropriate method
			
			// files[0] first
			if (result.hugeDataFlag) {
				write_only_huge_data(result.fOutputStream, result.fInputStream, 0, temp);
			} else {
				result.fOutputStream.write(result.data);
			}
			// then the rest
			for (int i = 1; i < files.length; i++) {
				if (files[i].hugeDataFlag) {
					write_only_huge_data(result.fOutputStream, files[i].fInputStream, 0, files[i].dataSize);
				} else {
					result.fOutputStream.write(files[i].data);
				}
			}
			
			// close streams
			for (WAVFile wavFile : files) {
				if (wavFile.fInputStream != null) {
					wavFile.fInputStream.close();
				}
				
				if (wavFile.fOutputStream != null) {
					wavFile.fOutputStream.close();
				}			
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/* Input: close- should we close stream in this method after writing? || Output: returns nothing, creates on disk new WAV file with values from fields*/
	private void write_all(boolean close) {

		try {
			fOutputStream = new FileOutputStream(filePath + "/_edited_" + fileName);

			fOutputStream.write("RIFF".getBytes());
			fOutputStream.write(ByteUtils.toLittleEndianByteArray((dataSize + CANON_HEADER_SIZE) - 8, 4));
			fOutputStream.write("WAVE".getBytes());
			fOutputStream.write("fmt ".getBytes());
			fOutputStream.write(ByteUtils.toLittleEndianByteArray(formatChunkSize, 4));
			fOutputStream.write(ByteUtils.toLittleEndianByteArray(format, 2));
			fOutputStream.write(ByteUtils.toLittleEndianByteArray(channelsNumber, 2));
			fOutputStream.write(ByteUtils.toLittleEndianByteArray(samplerate, 4));
			fOutputStream.write(ByteUtils.toLittleEndianByteArray(bitrate, 4));
			fOutputStream.write(ByteUtils.toLittleEndianByteArray(blockAlign, 2));
			fOutputStream.write(ByteUtils.toLittleEndianByteArray(bitsPerSample, 2));
			fOutputStream.write("data".getBytes());
			fOutputStream.write(ByteUtils.toLittleEndianByteArray(dataSize, 4));
			fOutputStream.write(data);

			if (close) {
				fInputStream.close();
				fOutputStream.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* Input: nothing || Output: returns nothing, creates on disk new WAV file with values from fields, EXCEPT SOUND DATA*/
	private void write_only_header() {

		try {
			fOutputStream = new FileOutputStream(filePath + "/_edited_" + fileName);

			fOutputStream.write("RIFF".getBytes());
			fOutputStream.write(ByteUtils.toLittleEndianByteArray((dataSize + CANON_HEADER_SIZE) - 8, 4));
			fOutputStream.write("WAVE".getBytes());
			fOutputStream.write("fmt ".getBytes());
			fOutputStream.write(ByteUtils.toLittleEndianByteArray(formatChunkSize, 4));
			fOutputStream.write(ByteUtils.toLittleEndianByteArray(format, 2));
			fOutputStream.write(ByteUtils.toLittleEndianByteArray(channelsNumber, 2));
			fOutputStream.write(ByteUtils.toLittleEndianByteArray(samplerate, 4));
			fOutputStream.write(ByteUtils.toLittleEndianByteArray(bitrate, 4));
			fOutputStream.write(ByteUtils.toLittleEndianByteArray(blockAlign, 2));
			fOutputStream.write(ByteUtils.toLittleEndianByteArray(bitsPerSample, 2));
			fOutputStream.write("data".getBytes());
			fOutputStream.write(ByteUtils.toLittleEndianByteArray(dataSize, 4));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* Input: targetOutputStream- stream to write to, targetInputStream- stream to read from, from- how much bytes to skip before writing, amount- how much bytes to write ||
	 * Output: returns nothing, takes selected data from passed input stream and using custom buffer writes it to output stream*/ 
	static private void write_only_huge_data(FileOutputStream targetOutputStream, FileInputStream targetInputStream, long from, long amount) {
		
		// when data is too big lets use custom buffer
		byte[] tempbuffer = new byte[CUSTOM_BUFFER_SIZE];

		// === writing huge data ===
		try {
			targetInputStream.skip(from);
			// first batches of array data
			while (amount > CUSTOM_BUFFER_SIZE) {
				targetInputStream.read(tempbuffer);
				targetOutputStream.write(tempbuffer);
				amount -= CUSTOM_BUFFER_SIZE;
			}
			//when we leave the loop we know, we are in int range
			tempbuffer = new byte[(int) amount];

			targetInputStream.read(tempbuffer);
			targetOutputStream.write(tempbuffer);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// trivial methods
	
	// prints info about file to standard output
	public void stdPrintInfo() {
		//quatity of appendable data- 13
		StringBuffer buffer = new StringBuffer(13);
		
		buffer.append("NAME: " + fileName);
		buffer.append("\nfile size: " + fileSize + 8);
		buffer.append("\nformat chunk size: " + formatChunkSize);
		buffer.append("\nformat: " + format);
		buffer.append("\nchannels number: " + channelsNumber);
		buffer.append("\nsamplerate: " + samplerate);
		buffer.append("\nbitrate: " + bitrate);
		buffer.append("\nblock align: " + blockAlign);
		buffer.append("\nbits per sample: " + bitsPerSample);
		buffer.append("\ndata size: " + dataSize);
		buffer.append("\n==========================");
		buffer.append("\nbytes per second: " + bytesPerSec);
		buffer.append("\nhuge file(can't be loaded into memory as whole)? " + hugeDataFlag);
		
		System.out.println(buffer.toString()+"\n");
	}

	public String getFileName() {
		return fileName;
	}

	public int getAudioLength() {
		return (int) (dataSize / bytesPerSec);
	}

}
