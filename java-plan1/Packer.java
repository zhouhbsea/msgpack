import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.math.BigInteger;

public class Packer {
	protected byte[] castBytes = new byte[9];
	protected ByteBuffer castBuffer = ByteBuffer.wrap(castBytes);
	protected OutputStream out;

	public Packer(OutputStream out)
	{
		this.out = out;
	}

	public Packer packByte(byte d) throws IOException
	{
		if(d < -(1<<5)) {
			castBytes[0] = (byte)0xd1;
			castBytes[1] = d;
			out.write(castBytes, 0, 2);
		} else {
			out.write(d);
		}
		return this;
	}

	public Packer packShort(short d) throws IOException
	{
		if(d < -(1<<5)) {
			if(d < -(1<<7)) {
				// signed 16
				castBytes[0] = (byte)0xd1;
				castBuffer.putShort(1, d);
				out.write(castBytes, 0, 3);
			} else {
				// signed 8
				castBytes[0] = (byte)0xd0;
				castBytes[1] = (byte)d;
				out.write(castBytes, 0, 2);
			}
		} else if(d < (1<<7)) {
			// fixnum
			out.write((byte)d);
		} else {
			if(d < (1<<8)) {
				// unsigned 8
				castBytes[0] = (byte)0xcc;
				castBytes[1] = (byte)d;
				out.write(castBytes, 0, 2);
			} else {
				// unsigned 16
				castBytes[0] = (byte)0xcd;
				castBuffer.putShort(1, d);
				out.write(castBytes, 0, 3);
			}
		}
		return this;
	}

	public Packer packInt(int d) throws IOException
	{
		if(d < -(1<<5)) {
			if(d < -(1<<15)) {
				// signed 32
				castBytes[0] = (byte)0xd2;
				castBuffer.putInt(1, d);
				out.write(castBytes, 0, 5);
			} else if(d < -(1<<7)) {
				// signed 16
				castBytes[0] = (byte)0xd1;
				castBuffer.putShort(1, (short)d);
				out.write(castBytes, 0, 3);
			} else {
				// signed 8
				castBytes[0] = (byte)0xd0;
				castBytes[1] = (byte)d;
				out.write(castBytes, 0, 2);
			}
		} else if(d < (1<<7)) {
			// fixnum
			out.write((byte)d);
		} else {
			if(d < (1<<8)) {
				// unsigned 8
				castBytes[0] = (byte)0xcc;
				castBytes[1] = (byte)d;
				out.write(castBytes, 0, 2);
			} else if(d < (1<<16)) {
				// unsigned 16
				castBytes[0] = (byte)0xcd;
				castBuffer.putShort(1, (short)d);
				out.write(castBytes, 0, 3);
			} else {
				// unsigned 32
				castBytes[0] = (byte)0xce;
				castBuffer.putInt(1, d);
				out.write(castBytes, 0, 5);
			}
		}
		return this;
	}

	public Packer packLong(long d) throws IOException
	{
		if(d < -(1L<<5)) {
			if(d < -(1L<<15)) {
				if(d < -(1L<<31)) {
					// signed 64
					castBytes[0] = (byte)0xd3;
					castBuffer.putLong(1, d);
					out.write(castBytes, 0, 9);
				} else {
					// signed 32
					castBytes[0] = (byte)0xd2;
					castBuffer.putInt(1, (int)d);
					out.write(castBytes, 0, 5);
				}
			} else {
				if(d < -(1<<7)) {
					// signed 16
					castBytes[0] = (byte)0xd1;
					castBuffer.putShort(1, (short)d);
					out.write(castBytes, 0, 3);
				} else {
					// signed 8
					castBytes[0] = (byte)0xd0;
					castBytes[1] = (byte)d;
					out.write(castBytes, 0, 2);
				}
			}
		} else if(d < (1<<7)) {
			// fixnum
			out.write((byte)d);
		} else {
			if(d < (1L<<16)) {
				if(d < (1<<8)) {
					// unsigned 8
					castBytes[0] = (byte)0xcc;
					castBytes[1] = (byte)d;
					out.write(castBytes, 0, 2);
				} else {
					// unsigned 16
					castBytes[0] = (byte)0xcd;
					castBuffer.putShort(1, (short)d);
					out.write(castBytes, 0, 3);
					//System.out.println("pack uint 16 "+(short)d);
				}
			} else {
				if(d < (1L<<32)) {
					// unsigned 32
					castBytes[0] = (byte)0xce;
					castBuffer.putInt(1, (int)d);
					out.write(castBytes, 0, 5);
				} else {
					// unsigned 64
					castBytes[0] = (byte)0xcf;
					castBuffer.putLong(1, d);
					out.write(castBytes, 0, 9);
				}
			}
		}
		return this;
	}

	public Packer packFloat(float d) throws IOException
	{
		castBytes[0] = (byte)0xca;
		castBuffer.putFloat(1, d);
		out.write(castBytes, 0, 5);
		return this;
	}

	public Packer packDouble(double d) throws IOException
	{
		castBytes[0] = (byte)0xcb;
		castBuffer.putDouble(1, d);
		out.write(castBytes, 0, 9);
		return this;
	}

	public Packer packNil() throws IOException
	{
		out.write((byte)0xc0);
		return this;
	}

	public Packer packTrue() throws IOException
	{
		out.write((byte)0xc3);
		return this;
	}

	public Packer packFalse() throws IOException
	{
		out.write((byte)0xc2);
		return this;
	}

	public Packer packArray(int n) throws IOException
	{
		if(n < 16) {
			final int d = 0x90 | n;
			out.write((byte)d);
		} else if(n < 65536) {
			castBytes[0] = (byte)0xdc;
			castBuffer.putShort(1, (short)n);
			out.write(castBytes, 0, 3);
		} else {
			castBytes[0] = (byte)0xdd;
			castBuffer.putInt(1, n);
			out.write(castBytes, 0, 5);
		}
		return this;
	}

	public Packer packMap(int n) throws IOException
	{
		if(n < 16) {
			final int d = 0x80 | n;
			out.write((byte)d);
		} else if(n < 65536) {
			castBytes[0] = (byte)0xde;
			castBuffer.putShort(1, (short)n);
			out.write(castBytes, 0, 3);
		} else {
			castBytes[0] = (byte)0xdf;
			castBuffer.putInt(1, n);
			out.write(castBytes, 0, 5);
		}
		return this;
	}

	public Packer packRaw(int n) throws IOException
	{
		if(n < 32) {
			final int d = 0xa0 | n;
			out.write((byte)d);
		} else if(n < 65536) {
			castBytes[0] = (byte)0xda;
			castBuffer.putShort(1, (short)n);
			out.write(castBytes, 0, 3);
		} else {
			castBytes[0] = (byte)0xdb;
			castBuffer.putInt(1, n);
			out.write(castBytes, 0, 5);
		}
		return this;
	}

	public Packer packRawBody(byte[] b) throws IOException
	{
		out.write(b);
		return this;
	}

	public Packer packRawBody(byte[] b, int off, int length) throws IOException
	{
		out.write(b, off, length);
		return this;
	}

	//public Packer pack(Object o) throws IOException
}

