package VoiceTCP;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class VoiceClient {

    public boolean stopCapture = false;
    AudioFormat audioFormat1;
    AudioFormat audioFormat2;
    TargetDataLine targetDataLine;
    SourceDataLine sourceDataLine;
    BufferedOutputStream clientOut = null;
    BufferedInputStream clientIn = null;
    Socket clientSocket = null;
    Thread captureThread;
    Thread playThread;
    
    public static void main(String[] args) throws LineUnavailableException {
        VoiceClient voiceClient = new VoiceClient("192.168.2.104", 2000);
    }

    public VoiceClient(String serverIP, int port) throws LineUnavailableException {
        try {

            clientSocket = new Socket(serverIP, port);
            clientOut = new BufferedOutputStream(clientSocket.getOutputStream());
            clientIn = new BufferedInputStream(clientSocket.getInputStream());
            //Client thu âm và gửi đi
//            CaptureAudio();
            //Client nhận âm thanh và phát
            PlayAudio();
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    // phương thức thu âm và gửi đi
    private void CaptureAudio() {
        try {
            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
            audioFormat1 = getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat1);
            Mixer mixer = AudioSystem.getMixer(mixerInfo[3]);
            targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
            targetDataLine.open(audioFormat1);
            targetDataLine.start();
            captureThread = new CaptureThread();
            captureThread.start();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    class CaptureThread extends Thread {

        byte tempBuffer[] = new byte[10000];

        @Override
        public void run() {
            stopCapture = false;
            try {
                while (!stopCapture) {
                    int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                    clientOut.write(tempBuffer);
                }
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }
        }
    }

    // phương thức nhận và phát âm thanh
    private void PlayAudio() throws IOException, LineUnavailableException {
        audioFormat2 = getAudioFormat();
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat2);
        sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
        sourceDataLine.open(audioFormat2);
        sourceDataLine.start();
        playThread = new PlayThread();
        playThread.start();
    }

    class PlayThread extends Thread {

        byte tempBuffer[] = new byte[10000];

        @Override
        public void run() {
            try {
                while (clientIn.read(tempBuffer) != -1) {
                    sourceDataLine.write(tempBuffer, 0, 10000);

                }
            } catch (IOException ex) {
                Logger.getLogger(VoiceClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 44100.0F;
        int sampleSizeInBits = 16;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
