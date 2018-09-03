/*
	BASSenc_MP3 2.4 Java class
	Copyright (c) 2018 Un4seen Developments Ltd.

	See the BASSENC_MP3.CHM file for more detailed documentation
*/

package com.un4seen.bass;

public class BASSenc_MP3
{
	public static native int BASS_Encode_MP3_GetVersion();

	public static native int BASS_Encode_MP3_Start(int handle, String options, int flags, BASSenc.ENCODEPROCEX proc, Object user);
	public static native int BASS_Encode_MP3_StartFile(int handle, String options, int flags, String filename);

    static {
        System.loadLibrary("bassenc");
        System.loadLibrary("bassenc_mp3");
    }
}
