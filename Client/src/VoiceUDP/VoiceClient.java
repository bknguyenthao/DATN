package VoiceUDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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

    DatagramSocket clientSocket;
    DatagramPacket receiverPacket, sendPacket;
    AudioFormat audioFormat1;
    AudioFormat audioFormat2;
    TargetDataLine targetDataLine;
    SourceDataLine sourceDataLine;
    Thread captureThread;
    Thread playThread;
    boolean stopCapture = false;

    public static void main(String[] args) throws IOException, SocketException, LineUnavailableException {
        VoiceClient voiceClient = new VoiceClient();
    }

    public VoiceClient() throws SocketException, IOException, LineUnavailableException {
        clientSocket = new DatagramSocket(1996);
        // client gửi
//        CaptureAudio();
        //client nhận
        PlayAudio();
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
            try {
                while (true) {
                    int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                    sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length, InetAddress.getByName("192.168.2.100"), 1995);
                    clientSocket.send(sendPacket);
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
                while (true) {
                    receiverPacket = new DatagramPacket(tempBuffer, 10000);
                    clientSocket.receive(receiverPacket);
                    sourceDataLine.write(tempBuffer, 0, 10000);
                }
            } catch (IOException ex) {
                Logger.getLogger(VoiceClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // khởi tạo thông số audioformat
    private AudioFormat getAudioFormat() {
        float sampleRate = 44100.0F;
        int sampleSizeInBits = 16;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
