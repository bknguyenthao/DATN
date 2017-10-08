package RSA;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

public class GiaiThuat extends javax.swing.JFrame {

    private static final int BUFFER_SIZE = 32 * 1024;
    private BigInteger n, d, e;

    public BigInteger getN() {
        return n;
    }

    public BigInteger getD() {
        return d;
    }

    public BigInteger getE() {
        return e;
    }

    public void setN(BigInteger n) {
        this.n = n;
    }

    public void setD(BigInteger d) {
        this.d = d;
    }

    public void setE(BigInteger e) {
        this.e = e;
    }

    //Hàm băm nhận đầu vào là một đường dẫn đến file
    public BigInteger SHA(String addFile) throws Exception {
        BufferedInputStream file = new BufferedInputStream(new FileInputStream(addFile));
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        DigestInputStream in = new DigestInputStream(file, md);
        int i;
        byte[] buffer = new byte[BUFFER_SIZE];
        do {
            i = in.read(buffer, 0, BUFFER_SIZE);
        } while (i == BUFFER_SIZE);
        md = in.getMessageDigest();
        return new BigInteger(md.digest());
    }

    // hàm tạo khóa
    public void KeyRSA(int sizeKey) {
        SecureRandom r = new SecureRandom();
        BigInteger p = new BigInteger(sizeKey / 2, 100, r);
        BigInteger q = new BigInteger(sizeKey / 2, 100, r);
        n = p.multiply(q);//khóa công khai
        BigInteger m = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        boolean found = false;
        do {
            e = new BigInteger(sizeKey / 2, 50, r);//khóa công khai
            if (m.gcd(e).equals(BigInteger.ONE) && e.compareTo(m) < 0) {
                found = true;
            }
        } while (!found);
        d = e.modInverse(m);//khóa bí mật
    }

    // hàm mã hóa RSA
    public BigInteger Encrypt(BigInteger giatribam, BigInteger khoabimat, BigInteger khoacongkhai_N) {
        return giatribam.modPow(khoabimat, khoacongkhai_N);
    }

    // hàm giải mã RSA
    public BigInteger Decrypt(BigInteger chukyso, BigInteger khoacongkhai_E, BigInteger khoacongkhai_N) {
        return chukyso.modPow(khoacongkhai_E, khoacongkhai_N);//(chukyso^e)mod N
    }

    // hàm lưu dữ liệu bigInteger vào bộ nhớ
    public void SaveFile(File saveTo, BigInteger data, String name) throws FileNotFoundException {
        File file = new File(saveTo, name);
        FileOutputStream fos = new FileOutputStream(file);
        try (PrintWriter pw = new PrintWriter(fos)) {
            pw.println(data);
        }
    }

    // hàm xác định vị trí lưu 1 file trong bộ nhớ
    public File LocationSave() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }

    // hàm đọc file từ đường dẫn
    public String ReadFile(String addFile) {
        FileInputStream fis = null;
        BufferedReader reader = null;
        String kq = "";
        try {
            fis = new FileInputStream(addFile);
            reader = new BufferedReader(new InputStreamReader(fis));
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                kq = kq + line;
                line = reader.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GiaiThuat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GiaiThuat.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                reader.close();
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(GiaiThuat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return kq;
    }
}
