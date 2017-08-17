import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class WAVFile {

	static final int CANON_HEADER_SIZE = 44;

	static final int CUSTOM_BUFFER_SIZE = 1500000000;

	// ***utility fields
	private FileInputStream finputstream;
	private FileOutputStream foutputstream;
	private String filename;
	private String filepath;
	// ***fields contained in WAV file
	private long fsize; // long - cause even though datasize is on 4 bytes,
						// 'int' doesnt cover it all in all cases since its
						// signed
	private int formatchunksize;
	private int format;
	private int no_of_channels;
	private int samplerate;
	private int bitrate;
	private int blockalign;
	private int bitspersample;
	private long datasize; // same as file size
	private byte[] data;
	// ***fields calculated from those from file
	private int bytes_per_sec;
	private boolean hugedataflag;

	public WAVFile(File file) {

		try {
			filepath = file.getParent();
			filename = file.getName();
			finputstream = new FileInputStream(file);

			// 0//RIFF marker
			finputstream.skip(4);

			// 4//file size
			byte[] bArr = new byte[4];

			finputstream.read(bArr);

			fsize = ByteUtils.toLong_fromLittleEndian(bArr);

			// 8//format
			finputstream.skip(4);

			// 12//chunk id
			finputstream.skip(4);

			// 16//format chunk size
			bArr = new byte[4];

			finputstream.read(bArr);

			formatchunksize = ByteUtils.toInt_fromLittleEndian(bArr);

			// 20//audio format
			bArr = new byte[2];
			finputstream.read(bArr);

			format = ByteUtils.toInt_fromLittleEndian(bArr);

			// 22//no of channels
			bArr = new byte[2];
			finputstream.read(bArr);

			no_of_channels = ByteUtils.toInt_fromLittleEndian(bArr);

			// 24//sample rate
			bArr = new byte[4];
			finputstream.read(bArr);

			samplerate = ByteUtils.toInt_fromLittleEndian(bArr);

			// 28//byte rate
			bArr = new byte[4];
			finputstream.read(bArr);

			bitrate = ByteUtils.toInt_fromLittleEndian(bArr);

			// 32//BlockAlign
			bArr = new byte[2];
			finputstream.read(bArr);

			blockalign = ByteUtils.toInt_fromLittleEndian(bArr);

			// 34//BitsPerSample
			bArr = new byte[2];
			finputstream.read(bArr);

			bitspersample = ByteUtils.toInt_fromLittleEndian(bArr);

			// 36//data id
			////
			// data chunk location may vary in non-canon so we check
			////
			while (finputstream.available() > 0)
				if (finputstream.read() == 'd')
					if (finputstream.read() == 'a')
						if (finputstream.read() == 't')
							if (finputstream.read() == 'a')
								break;

			// 40//data size
			bArr = new byte[4];
			finputstream.read(bArr);

			datasize = ByteUtils.toLong_fromLittleEndian(bArr);

			// 44//END OF HEADER, now only sound data is left
			// calculating bps
			bytes_per_sec = (bitspersample * samplerate * no_of_channels) / 8;

			if (datasize <= Integer.MAX_VALUE) {
				try {
					data = new byte[(int) datasize];
				} catch (OutOfMemoryError e) {
					hugedataflag = true;
					return;
				}
				finputstream.read(data);

			} else {
				hugedataflag = true;
			}
			printInfo();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void cutFromTo(int from, int to) {
		// will document this shit later.......
		long from_bytes = (long)bytes_per_sec * from;
		long to_bytes = (long)bytes_per_sec * to;
		long diff = to_bytes - from_bytes;
	
		if (hugedataflag) {
			datasize = diff;
			write_all_but_data();
			write_only_huge_data(this.foutputstream, this.finputstream, from_bytes, diff);
			
			return;
		}

		int tmpfrom = (int) from_bytes;
		int tmpdiff = (int) diff;

		try {
			byte[] tempbuffer = new byte[tmpdiff];
			for (int i = tmpfrom; i < to_bytes; i++) {
				tempbuffer[i - tmpfrom] = data[i];
			}

			data = tempbuffer;

		} catch (OutOfMemoryError e) {
			// if OutOfMemory error occurs here it means previously there was no
			// OOM error -> two huge array are too big so we again read just to
			// operate only one array
			data = null;
			data = new byte[tmpdiff];
			try {
				finputstream = new FileInputStream(new File(filename));
				finputstream.skip(from_bytes);

				finputstream.read(data);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

		datasize = data.length;
		write_all(true);
	}

	public static void join(WAVFile[] files) {

		// first file as template for WAV file properties
		WAVFile result = files[0];
		// lets start by writing all what files[0] has to write and keep its output stream open
		// for storing datsize since we will need it unchanged in a loop
		long temp = result.datasize;
		for (int i = 1; i < files.length; i++) {
			result.datasize += files[i].datasize;
		}
		result.write_all_but_data();
		// append only data of files
		try {
			
			if (result.hugedataflag) {
				write_only_huge_data(result.foutputstream, result.finputstream, 0, temp);
			} else {
				result.foutputstream.write(result.data);
			}
			
			for (int i = 1; i < files.length; i++) {
				if (files[i].hugedataflag) {
					write_only_huge_data(result.foutputstream, files[i].finputstream, 0, files[i].datasize);
				} else {
					result.foutputstream.write(files[i].data);
				}
			}
			
			for (WAVFile wavFile : files) {
				wavFile.finputstream.close();
				wavFile.foutputstream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void write_all(boolean close) {

		try {
			foutputstream = new FileOutputStream(filepath + "/_edited_" + filename);

			foutputstream.write("RIFF".getBytes());
			foutputstream.write(ByteUtils.toLittleEndianByteArray((datasize + CANON_HEADER_SIZE) - 8, 4));
			foutputstream.write("WAVE".getBytes());
			foutputstream.write("fmt ".getBytes());
			foutputstream.write(ByteUtils.toLittleEndianByteArray(formatchunksize, 4));
			foutputstream.write(ByteUtils.toLittleEndianByteArray(format, 2));
			foutputstream.write(ByteUtils.toLittleEndianByteArray(no_of_channels, 2));
			foutputstream.write(ByteUtils.toLittleEndianByteArray(samplerate, 4));
			foutputstream.write(ByteUtils.toLittleEndianByteArray(bitrate, 4));
			foutputstream.write(ByteUtils.toLittleEndianByteArray(blockalign, 2));
			foutputstream.write(ByteUtils.toLittleEndianByteArray(bitspersample, 2));
			foutputstream.write("data".getBytes());
			foutputstream.write(ByteUtils.toLittleEndianByteArray(datasize, 4));
			foutputstream.write(data);

			if (close) {
				foutputstream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void write_all_but_data() {

		try {
			foutputstream = new FileOutputStream(filepath + "/_edited_" + filename);

			foutputstream.write("RIFF".getBytes());
			foutputstream.write(ByteUtils.toLittleEndianByteArray((datasize + CANON_HEADER_SIZE) - 8, 4));
			foutputstream.write("WAVE".getBytes());
			foutputstream.write("fmt ".getBytes());
			foutputstream.write(ByteUtils.toLittleEndianByteArray(formatchunksize, 4));
			foutputstream.write(ByteUtils.toLittleEndianByteArray(format, 2));
			foutputstream.write(ByteUtils.toLittleEndianByteArray(no_of_channels, 2));
			foutputstream.write(ByteUtils.toLittleEndianByteArray(samplerate, 4));
			foutputstream.write(ByteUtils.toLittleEndianByteArray(bitrate, 4));
			foutputstream.write(ByteUtils.toLittleEndianByteArray(blockalign, 2));
			foutputstream.write(ByteUtils.toLittleEndianByteArray(bitspersample, 2));
			foutputstream.write("data".getBytes());
			foutputstream.write(ByteUtils.toLittleEndianByteArray(datasize, 4));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static private void write_only_huge_data(FileOutputStream targetostream, FileInputStream targetistream, long from, long amount) {
		// when data is too big lets use custom buffer

		byte[] tempbuffer = new byte[CUSTOM_BUFFER_SIZE];

		// === writing huge data ===
		try {
			targetistream.skip(from);
			// first batches of array data
			while (amount > CUSTOM_BUFFER_SIZE) {
				targetistream.read(tempbuffer);
				targetostream.write(tempbuffer);
				amount -= CUSTOM_BUFFER_SIZE;
			}

			tempbuffer = new byte[(int) amount];

			targetistream.read(tempbuffer);
			targetostream.write(tempbuffer);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printInfo() {
		System.out.println("file size: " + fsize + 8);
		System.out.println("format chunk size: " + formatchunksize);
		System.out.println("format: " + format);
		System.out.println("no_of_channels: " + no_of_channels);
		System.out.println("samplerate: " + samplerate);
		System.out.println("bitrate: " + bitrate);
		System.out.println("blockalign: " + blockalign);
		System.out.println("bitspersample: " + bitspersample);
		System.out.println("datasize: " + datasize);
		System.out.println("==========================");
		System.out.println("bytes per sec: " + bytes_per_sec);
		System.out.println("huge file? " + hugedataflag);
	}

	public String getFilename() {
		return filename;
	}

	public int getAudioLength() {
		return (int) (datasize / bytes_per_sec);
	}

}
