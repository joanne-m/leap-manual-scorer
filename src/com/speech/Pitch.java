/**
 * ==============================================================================
 * File:			SpeechPitch.java
 * Created:			2013/09/04
 * Last Changed:	2013/09/04
 * Author:			Gay Figueroa
 * ==============================================================================
 * This code is copyright (c) 2013 Learning English Application for Pinoys (LEAP)
 * 
 * History:
 * 01/28/2014 gay - refactored
 * 
 */

package com.speech;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import flanagan.complex.Complex;
import flanagan.math.FourierTransform;
import flanagan.math.Matrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.util.Globals;

/**
 * Computes the pitch contour of a given signal using the auto-correlation. Each
 * SpeechPitch object contains only one value and its corresponding index in the
 * speech signal. To return the pitch contour of one long signal, a vector of
 * SpeechPitch is returned to the calling function.
 * 
 * {@link com.dostproject7.speech.Pitch SpeechPitch}
 * 
 * @author Gay Figueroa
 * @version 1.0
 * 
 */

public class Pitch {

	private double value;
	private int sampleIndex;
	/** Logger variable */
	private final static Logger LOGGER = LogManager.getLogger();

	public Pitch() {
		this.value = 0;
		this.sampleIndex = -1;
	}

	public Pitch(double value) {
		this.value = value;
	}

	public boolean isEmpty() {
		return this.value == 0 && this.sampleIndex < 0;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getSampleIndex() {
		return sampleIndex;
	}

	public void setSampleIndex(int sampleIndex) {
		this.sampleIndex = sampleIndex;
	}

	@Override
	public String toString() {
		return "(" + this.sampleIndex + ", " + (int) this.value + ")";
	}
	
	/**
	 * Returns <code>true</code> if the sample is unvoiced
	 * @return <code>true</code> if the sample is unvoiced; <code>false</code> otherwise
	 */
	public boolean isUnvoiced() {
		return this.value == 0;
	}

	/**
	 * Computes pitch per frame
	 * @param audioFile the filename of the wav file
	 * @return the pitch contour of the wav file
	 * @throws IOException if the file specified is of a different format
	 */
	public static List<Pitch> computePitch(File audioFile)
			throws IOException {
		try {
			if (!Globals.INSTALLER) {
				String path = audioFile.getPath().replace("..\\..\\", "");
				audioFile = new File(path);
			}
			LOGGER.debug("Computing the pitch contour of audio file {}",
					audioFile);
			/** read audio file */
			LOGGER.debug("Reading audio file to rawInputStream");
			AudioInputStream rawInputStream = AudioSystem
					.getAudioInputStream(audioFile);
			int rawFrameLength = (int) rawInputStream.getFrameLength();
			int formatFrameSize = rawInputStream.getFormat().getFrameSize();
			float formatSampleRate = rawInputStream.getFormat().getSampleRate();
			LOGGER.debug("> Frame Length = {}", rawFrameLength);
			LOGGER.debug("> Frame Size = {}", formatFrameSize);
			LOGGER.debug("> Number of samples = {} bytes",
					(rawFrameLength * formatFrameSize));
			LOGGER.debug("> Sample Rate = {}", formatSampleRate);

			byte[] audioByteData = new byte[rawFrameLength * formatFrameSize];
			rawInputStream.read(audioByteData);
			LOGGER.debug("Successfully read audio file");

			/** convert to byte samples to Short Integer */
			LOGGER.debug("Converting Byte data to Short Integer");
			ShortBuffer audioShortBuf = ByteBuffer.wrap(audioByteData)
					.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
			short[] audioShortIntData = new short[audioShortBuf.capacity()];
			audioShortBuf.get(audioShortIntData);

			LOGGER.debug("Bufferring data into matrix");
			LOGGER.debug("Frame length = 256 (30ms)");
			LOGGER.debug("Overlap = 96 (10ms)");

			/**
			 * 256 ms frame length --> 16000*0.03 = 480 (30 ms) 96 ms overlap
			 * --> 16000*0.02 = 320 (20ms) = 10ms step size
			 */
			int frameLength = 256;
			int overlap = 96;
			int appliedFrameLength = frameLength - overlap;
			int frameCount = (int) Math.ceil((double) audioShortIntData.length
					/ appliedFrameLength);
			LOGGER.debug("Total number of frames = {}", frameCount);

			/**
			 * initialize float array - short integer data will be converted to
			 * float
			 */
			float[] audioFloatData = new float[audioShortIntData.length];

			/**
			 * convert audio data into float then buffer audio data into matrix
			 * audioFLoatData is used as the container of the float data
			 */
			Matrix audioBuffer = Pitch.getAudioBufferMatrix(
					audioShortIntData, frameLength, overlap, frameCount,
					audioFloatData);
			LOGGER.debug("Successfully buffered data into matrix");

			/** compute fundamental frequency and pitch of audio samples */
			LOGGER.debug("Computing the pitch per frame");
			double f0[] = new double[frameCount];
			double threshold = 30;
			List<Pitch> pitchList = new ArrayList<Pitch>(
					frameCount + 10);

			/** get pitch of each frame and apply smoothing technique */
			for (int i = 0; i < frameCount; i++) {

				/** get ___ of frame i */
				Matrix rowMatrix = Matrix.rowMatrix(audioBuffer.getRowCopy(i));

				/** get pitch per frame, save to a SpeechPitch object */
				f0[i] = Pitch.computePitchValue(frameLength,
						formatSampleRate, rowMatrix);
				if (f0[i] == -1) {
					LOGGER.warn("Unable to compute the pitch of signal in frame {}");
					LOGGER.warn("The pitch value in frame {} is set to 0");
					f0[i] = 0;
				}
				Pitch pitchObject = new Pitch(f0[i]);
				pitchObject.setSampleIndex(i);
				pitchList.add(pitchObject);

				/**
				 * Apply smoothing technique: if difference in pitch is more
				 * than the threshold apply smoothing technique to frame i-1
				 * based on frames i, i-1 and i-2
				 * */
				if (i > 1 && i < frameCount - 1) {
					if (Math.abs(f0[i] - f0[i - 1]) >= threshold
							|| Math.abs(f0[i - 2] - f0[i - 1]) >= threshold) {
						Pitch newPitch = pitchList.get(i - 1);
						newPitch.setValue(Pitch.applySmoothing(f0[i],
								f0[i - 2], f0[i - 2]));
						pitchList.set(i - 1, newPitch);
					}
				}

			}
			LOGGER.debug("Done computing the pitch contour of audio file {}",
					audioFile);
			return pitchList;
		} catch (UnsupportedAudioFileException e) {
			LOGGER.error("Unsupported Audio File Exception", e);
			throw new IOException("UnsupportedAudioFileException");
		}
	}

	private static Matrix getAudioBufferMatrix(short[] audioShortIntData,
			int frameLength, int overlap, int frameCount, float[] audioFloatData) {

		LOGGER.debug("converting audio data to float and buffering the signal into matrix");
		Matrix audioBufferMatrix = new Matrix(frameCount, frameLength);
		int appliedFrameLength = frameLength - overlap;
		int audioDataLength = audioShortIntData.length;
		int sampleIndex = 0;
		for (int i = 0; i < frameCount; i++) {
			int j = 0;

			while (j < overlap && j < frameLength) {
				if (i == 0) {
					audioBufferMatrix.setElement(i, j, 0.0);
				} else {
					double overlapFrameValue = audioBufferMatrix.getElement(
							i - 1, appliedFrameLength + j);
					audioBufferMatrix.setElement(i, j, overlapFrameValue);
				}
				j++;
			}

			while (sampleIndex < audioDataLength && j < frameLength) {
				float audioFloatValue = ((float) audioShortIntData[sampleIndex]) / 0x8000;
				audioFloatData[sampleIndex] = audioFloatValue;
				audioBufferMatrix.setElement(i, j, audioFloatValue);
				sampleIndex++;
				j++;
			}

			while (j < frameLength) {
				audioBufferMatrix.setElement(i, j, 0.0);
				sampleIndex++;
				j++;
			}

		}
		LOGGER.debug("Done converting audio data to float and buffering the signal into matrix");
		return audioBufferMatrix;
	}

	private static double computePitchValue(int frameLength, float sampleRate,
			Matrix rowMatrix) {
		/**
		 * computes the pitch of one frame of a speech signal. no logger was
		 * placed here since this is called many times for one audio file
		 */
		try {
			/** find the clipping level */

			/** get the location of A's right boundary in the frame */
			int i13 = (int) Math.floor(frameLength / 3);
			/**
			 * get the location of C's starting point or left boundary in the
			 * frame
			 */
			int i23 = (int) Math.floor(2 * frameLength / 3) - 1;

			int[] tmp1 = rowMatrix.getSubMatrix(0, 0, 0, i13 - 1).pivot();
			int[] tmp2 = rowMatrix.getSubMatrix(0, i23, 0, frameLength - 1)
					.pivot();

			/**
			 * the clipping level is set at the 68% of the smaller value between
			 * between the peak absolute sample values in A and C of this frame
			 */
			double CL = 0.0;
			if (rowMatrix.getElement(tmp1[0], tmp1[1]) > rowMatrix.getElement(
					tmp2[0], i23 + tmp2[1]))
				CL = 0.68 * rowMatrix.getElement(tmp2[0], tmp2[1]);
			else
				CL = 0.68 * rowMatrix.getElement(tmp1[0], tmp1[1]);

			/** non-linear center clipping */
			double[] clip = rowMatrix.getRowCopy(0);
			if (CL > 0) {
				for (int i = 0; i < frameLength; i++) {
					if (clip[i] >= CL)
						clip[i] = clip[i] - CL;
					else if (clip[i] <= -CL)
						clip[i] = clip[i] + CL;
				}
			}

			/**
			 * compute the autocorrelation function at 0 delay for appropriate
			 * normalization. basically RR describes the frequency content of
			 * the speech signal
			 */

			double[] RR = xcorr(clip);
			/** find the position value of autocorrelation peak */
			double LF = Math.floor(sampleRate / 500);
			double HF = Math.floor(sampleRate / 75);

			/**
			 * find the max autocorrelation / autocorrelation peak in the range
			 * 60 to 320 (inclusive)
			 */
			Matrix Rxx = Matrix.rowMatrix(RR).getSubMatrix(0,
					frameLength + (int) LF - 1, 0, frameLength + (int) HF - 1);
			RR = null;
			RR = Rxx.maximumElement();

			/** p is f0 */
			double p = 0.0;
			p = RR[2] + LF;
			p = sampleRate / p;

			/**
			 * compare peak value with voiced/unvoiced threshold to
			 * differentiate silence from speech signal based on energy
			 */
			double energy = norm2(clip);
			double sil = 0.40 * energy;

			/**
			 * if it's above the silence value and between 60 to 320, it's
			 * voiced
			 */
			if ((RR[0] > sil) && (p > 60) && (p <= 320)) {
				return p;
			}
			/** otherwise, it's unvoiced */
			return 0;
		} catch (Exception e) {
			LOGGER.error("Error in computing pitch. Return -1 to signal error",
					e);
			return -1;
		}
	}

	public static double norm2(double[] m) {
		/** gets the normal of a signal or the sum of the square of the signal */
		double norm = 0.0D;
		for (int i = 0; i < m.length; i++) {
			norm = norm + Math.pow(m[i], 2);
		}
		return norm;
	}

	/**
	 * Cross-correlation between 2 signals tells how identical the signals are
	 * If there is a correlation, the signals are somehow dependent on each
	 * other. Correlation value is computed with different alignments called
	 * lags between the signals
	 * 
	 * Autocorrelation is cross-correlation of a signal with itself. The
	 * autocorrelation value on lag 0 is equal to the energy of the signal.
	 */

	public static double[] xcorr(double[] m) {
		double[] RR = new double[512];
		System.arraycopy(m, 0, RR, 0, 256);
		FourierTransform ft = new FourierTransform();
		ft.setData(RR);
		ft.transform();
		Complex[] fft = ft.getTransformedDataAsComplex();
		for (int i = 0; i < 512; i++) {
			fft[i].setReal(fft[i].squareAbs());
			fft[i].setImag(0);
		}
		RR = absifft(fft);
		return RR;
	}

	/** Fast Fourier Transform */
	public static void inplaceFFT(Complex[] x) {
		/** check that length is a power of 2 */
		int N = x.length;
		if (Integer.highestOneBit(N) != N) {
			throw new RuntimeException("N is not a power of 2");
		}

		/** bit reversal permutation */
		int shift = 1 + Integer.numberOfLeadingZeros(N);
		for (int k = 0; k < N; k++) {
			int j = Integer.reverse(k) >>> shift;
			if (j > k) {
				Complex temp = x[j];
				x[j] = x[k];
				x[k] = temp;
			}
		}

		/** butterfly updates */
		for (int L = 2; L <= N; L = L + L) {
			for (int k = 0; k < L / 2; k++) {
				double kth = -2 * k * Math.PI / L;
				Complex w = new Complex(Math.cos(kth), Math.sin(kth));
				for (int j = 0; j < N / L; j++) {
					Complex tao = w.times(x[j * L + k + L / 2]);
					x[j * L + k + L / 2] = x[j * L + k].minus(tao);
					x[j * L + k] = x[j * L + k].plus(tao);
				}
			}
		}
	}

	/**
	 * abs(inverse fft(x)) in matlab
	 */

	public static double[] absifft(Complex[] x) {
		int N = x.length;
		Complex[] y = new Complex[N];
		double[] z = new double[N];

		/** take the conjugate of a signal */
		for (int i = 0; i < N; i++) {
			y[i] = x[i].conjugate();
		}
		/** compute forward FFT */
		inplaceFFT(y);

		for (int i = 0; i < 255; i++) {
			y[257 + i] = y[257 + i].conjugate();
			z[i] = y[257 + i].times(1.0 / N).getReal();
		}
		for (int i = 0; i < 256; i++) {
			y[i] = y[i].conjugate();
			z[255 + i] = y[i].times(1.0 / N).getReal();
		}
		return z;

	}

	/** smoothing technique applied to pitch contour */

	private static double applySmoothing(double f0i, double f0i1, double f0i2) {
		if (f0i1 == f0i / 2)
			return (f0i / 2);
		else if (f0i1 == f0i2 / 2)
			return (f0i2 / 2);
		else
			return 0;
	}

}
