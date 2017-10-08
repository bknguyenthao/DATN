package VideoUDP;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

public class Statics {

    public static String SERVER_IP = "";
    public static String CLIENT_IP = "";
    public static String keyAES = "1234567891234567";
    public static String IV = "AAAAAAAAAAAAAAAA";

    public static byte[] encryptLageFileAES(byte[] data, String encryptionKey, String IV) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV.getBytes("UTF-8")));

        int dem = 0;
        int l = data.length;
        byte[] result = new byte[0];
        byte[] bufferData = new byte[16];
        byte[] bufferEncrypt = new byte[16];
        byte[] bufferFinal = new byte[16];
        for (int i = 0; i < bufferFinal.length; i++) {
            bufferFinal[i] = 0;
        }
        if (l < 16) {
            System.arraycopy(data, 0, bufferFinal, 0, l);
            result = cipher.doFinal(bufferFinal);
        }
        if (l == 16) {
            result = cipher.doFinal(data);
        }

        if (l > 16) {
            for (int i = 0; i <= l; i++) {
                if ((i % 16 == 0) && (i + 16 < l)) {
//                    System.arraycopy(data, i, bufferData, 0, i+16);
                    bufferData = Arrays.copyOfRange(data, i, i + 16);
                    bufferEncrypt = cipher.doFinal(bufferData);
                    result = append(result, bufferEncrypt);
                    dem = dem + 16;

                }
            }
            // trường hợp vẫn còn dư một vài phần tử cuối cùng của mảng data
            if (dem < l) {
                System.arraycopy(data, dem, bufferFinal, 0, l - dem);
                bufferEncrypt = cipher.doFinal(bufferFinal);
                result = append(result, bufferEncrypt);
            }
        }
        return result;
    }

    public static byte[] decryptLageFileAES(byte[] data, String encryptionKey, String IV) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV.getBytes("UTF-8")));

        int l = data.length;
        byte[] result = new byte[0];
//        byte[] resultFinal = new byte[0];
//        int stop = 0;
        byte[] bufferData = new byte[16];
        byte[] bufferEncrypt = new byte[16];

        for (int i = 0; i <= l; i++) {
            if ((i % 16 == 0) && (i + 16 <= l)) {
                bufferData = Arrays.copyOfRange(data, i, i + 16);
                bufferEncrypt = cipher.doFinal(bufferData);
                result = append(result, bufferEncrypt);
            }
        }
        // xóa những phần tử 0 thừa ở cuối mảng
//        for (int i = 0; i <= result.length; i++) {
//            if (result[i] == 0) {
//                stop = i;
//                break;
//            }
//        }
//        resultFinal = Arrays.copyOfRange(result, 0, stop);
        return result;
    }

    public static byte[] append(byte[] prefix, byte[] suffix) {
        byte[] toReturn = new byte[prefix.length + suffix.length];
        System.arraycopy(prefix, 0, toReturn, 0, prefix.length);
        System.arraycopy(suffix, 0, toReturn, prefix.length, suffix.length);
        return toReturn;
    }

    public static byte[] getBytesFromBufferedImage(BufferedImage bImage) {
        byte[] imageInByte = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bImage, "jpg", baos);
            baos.flush();
            imageInByte = baos.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(Statics.class.getName()).log(Level.SEVERE, null, ex);
        }
        return imageInByte;
    }

    public static BufferedImage getBufferedImageFromByte(byte[] bytes) throws IOException {
        InputStream in = new ByteArrayInputStream(bytes);
        BufferedImage bImageFromConvert = ImageIO.read(in);
        return bImageFromConvert;
    }
}
