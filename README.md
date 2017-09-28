# AudioManip

WAV file manipulator

supported modes:
1) GUI SPLIT MODE

$ AudioManip.jar 

2) command line SPLIT MODE (creates new file as specified by ranges of HH:MM:SS)

$ AudioManip.jar -split file_to_split HH:MM:SS HH:MM:SS

3) command line JOIN MODE (joins files via their order of passing)

$ AudioManip.jar -join file1 file2 file...

4) command line INFO MODE (prints info about file to standard output device)

$ AudioManip.jar -info file1 file2 file...

5) command line BYTE SPLIT MODE (find out byte values through INFO MODE)

$ AudioManip.jar -bytesplit file_to_split from_bytes to_bytes