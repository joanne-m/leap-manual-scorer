package com.util;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;


// To play sound using Clip, the process need to be alive.
public class Playback implements Runnable {

	private Clip clip;
	private String filename;
	private File soundFile;
	private AudioInputStream soundIn;
	private AudioFormat format;
	private DataLine.Info info;	
	

	// Constructor
	public Playback() {
	}
	
	public void setFile(String filename) {
		this.filename = filename;
	}

	public void run() {
		try {

			soundFile = new File(filename);
			soundIn = AudioSystem.getAudioInputStream(soundFile);
 			format = soundIn.getFormat();
			info = new DataLine.Info(Clip.class, format);
			clip = (Clip) AudioSystem.getLine(info);
			clip.open(soundIn);
			
			clip.start();
			while (!clip.isRunning())
			    Thread.sleep(10);
			while (clip.isRunning())
			    Thread.sleep(10);
			clip.close();
		} catch (FileNotFoundException e) {
			System.out.println("Unable to play audio file {}"+ filename+ e);
			JOptionPane.showMessageDialog(null, "File not found: "+filename);
		} catch (LineUnavailableException e) {
			System.out.println("Unable to play audio file {}"+ filename+ e);
		} catch (UnsupportedAudioFileException e) {
			System.out.println("Unable to play audio file {}"+ filename+ e);
		} catch (IOException e) {
			System.out.println("Unable to play audio file {}"+ filename+ e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isPlaying() {
		if (clip != null) {
			return clip.isRunning();
		}
		return true;
	}

	public static InputStream loadStream(InputStream in) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int c;
		while ((c = in.read(buf)) != -1) {
			bos.write(buf, 0, c);
		}
		return new ByteArrayInputStream(bos.toByteArray());
	}

	public void stop() {
		clip.stop();
	}
	/*
	 * // Listens for when the clip has stopped playing and closes it. (Part of
	 * the LineListener) public void update(LineEvent event) { if
	 * (event.getType().equals(LineEvent.Type.STOP)) { hasStopped = true;
	 * hasStopped(); clip.close(); } }
	 */

	public boolean isNull() {
		// TODO Auto-generated method stub
		return (filename==null);
	}

}