import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class WAVFile {

	static final int CANON_HEADER_SIZE = 44;

	// ***utility fields
	private FileInputStream finputstream;
	private FileOutputStream foutputstream;
	private String filename;
	private String filepath;
	// ***fields contained in WAV file
	private int fsize;
	private int formatchunksize;
	private int format;
	private int no_of_channels;
	private int samplerate;
	private int bitrate;
	private int blockalign;
	private int bitspersample;
	private int datasize;
	private byte[] data;
	// ***fields calculated from those from file
	private int bytes_per_sec;



	public WAVFile(File file) {
		
		try {
			filepath = file.getParent();
			filename = file.getName();
			finputstream = new FileInputStream(file);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {

			// 0//RIFF marker
			finputstream.skip(4);

			// 4//file size
			byte[] bArr = new byte[4];

			finputstream.read(bArr);

			fsize = ByteUtils.toInt_fromLittleEndian(bArr);

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

			datasize = ByteUtils.toInt_fromLittleEndian(bArr);

			// 44//END OF HEADER, now only sound data is left
			data = new byte[datasize];
			finputstream.read(data);

			// calculating bps
			bytes_per_sec = (bitspersample * samplerate * no_of_channels) / 8;

			// closing input stream
			finputstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void cutFromTo(int from, int to) {

		int from_bytes = bytes_per_sec * from;
		int to_bytes = bytes_per_sec * to;
		int diff = to_bytes - from_bytes;

		byte[] tempbuffer = new byte[diff];
		for (int i = from_bytes; i < to_bytes; i++) {
			tempbuffer[i - from_bytes] = data[i];
		}

		data = tempbuffer;

		datasize = data.length;
		write_all(true);
	}

	public static void join(WAVFile[] files) {

		// first file as template for WAV file properties
		WAVFile result = files[0];
		// lets start by writing all what files[0] has to write and keep its output stream open
		for (int i = 1; i < files.length; i++) {
			result.datasize += files[i].datasize;
		}
		result.write_all(false);
		// append only data of remaining files
		try {
			for (int i = 1; i < files.length; i++) {
				result.foutputstream.write(files[i].data);
			}
			
		result.foutputstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void write_all(boolean close) {

		try {
			foutputstream = new FileOutputStream(filepath+"/_edited_"+filename);

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
	}

	public String getFilename() {
		return filename;
	}

	public int getAudioLength() {
		return datasize / bytes_per_sec;
	}

}
