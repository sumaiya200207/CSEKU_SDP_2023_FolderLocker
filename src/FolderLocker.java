import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Scanner;

public class FolderLocker {
    static String INSTALLATION_PATH;
    static String FOLDER_PATH;
    static String FILE_NAME;
    static String LOCKED_ICON_PATH;
    private final SecurityHandler handler;

    JFrame frame = new JFrame();
    JLabel labelNewPassword = new JLabel("New Password      ");
    JLabel labelConfirmPassword = new JLabel("Confirm Password");
    JLabel labelEnterPassword = new JLabel("Enter Password  ");

    JLabel responseMessage = new JLabel();
    JLabel infoMessage = new JLabel("Confirm password before removing.");
    JLabel dirName = new JLabel();

    JPasswordField passFieldNew = new JPasswordField(20);
    JPasswordField passFieldConfirm = new JPasswordField(20);
    JPasswordField passFieldEnter = new JPasswordField(20);

    JButton lockButton = new JButton("Lock");
    JButton unlockButton = new JButton("Unlock");
    JButton removeButton = new JButton("Remove password");

    public FolderLocker(
            String title,
            int width,
            int height,
            String installPath,
            String folderDirectory,
            String password_File_Name,
            String lockedIconPath) {
        INSTALLATION_PATH = installPath;
        FOLDER_PATH = folderDirectory;
        FILE_NAME = password_File_Name;
        LOCKED_ICON_PATH = lockedIconPath;
        this.handler = new SecurityHandler(folderDirectory);
        frame.setTitle(title);
        frame.setLayout(new GridLayout(5, 1));
        frame.setSize(width, height);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public String encrypt(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes(), 0, password.length());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException ignored) {
        }
        return null;
    }

    public void storePassword(String password, String key) {
        try {
            File file = new File(INSTALLATION_PATH, FILE_NAME);
            if (!file.exists()) {
                file.createNewFile();
            }

            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));
            boolean directoryFound = false;
            String line;

            while ((line = br.readLine()) != null) {
                String[] data = line.split("@");
                if (data[0].equals(FOLDER_PATH)) {
                    directoryFound = true;
                    line = FOLDER_PATH + "@" + encrypt(password) + "@" + key;
                }
                sb.append(line);

                if (line.length() > 0)
                    sb.append("\n");
            }

            if (!directoryFound) {
                line = FOLDER_PATH + "@" + encrypt(password) + "@" + key;
                sb.append(line).append("\n");
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(sb.toString());
            bw.close();
        } catch (IOException e) {
            System.err.println("An error occurred while storing password: " + e.getMessage());
        }
    }

    public void removePassword() throws IOException {
        File passwordFile = new File(INSTALLATION_PATH, FILE_NAME);
        FileReader fileReader = new FileReader(passwordFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuffer stringBuffer = new StringBuffer();
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            String[] data = line.split("@");
            if (data[0].equals(FOLDER_PATH)) {
                line = "";
                break;
            }
            stringBuffer.append(line);
            stringBuffer.append("\n");
        }

        handler.decrypt(handler.extractKey(extractPassword()[1]));

        for (File file : Objects.requireNonNull(new File(FOLDER_PATH).listFiles((dir, name) -> name.endsWith(".enc")))) {
            if (!file.delete()) {
                System.out.println("Failed to delete " + file.getAbsolutePath());
            } else {
                System.out.println("Files deleted successfully.");
            }
        }

        bufferedReader.close();
        FileWriter fileWriter = new FileWriter(FILE_NAME);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(stringBuffer.toString());
        bufferedWriter.close();

        System.out.println("Password Removed!");
        System.exit(0);
    }

    public String[] extractPassword() {
        try {
            File file = new File(INSTALLATION_PATH, FILE_NAME);
            try (Scanner Reader = new Scanner(file)) {
                while (Reader.hasNextLine()) {
                    String[] data = Reader.nextLine().split("@");

                    if (FOLDER_PATH.equals(data[0])) {
                        return new String[] {
                                data[1], data[2]
                        };
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
        return null;
    }

    public boolean isLocked() {
        try {
            File file = new File(INSTALLATION_PATH, FILE_NAME);

            if (!file.exists()) {
                file.createNewFile();
            }
            try (Scanner Reader = new Scanner(file)) {
                while (Reader.hasNextLine()) {
                    String[] data = Reader.nextLine().split("@");

                    if (FOLDER_PATH.equals(data[0])) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public void addComponents(boolean locked, String command) {
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        row1.add(new JLabel());
        row1.add(dirName);
        row1.add(new JLabel());
        frame.add(row1);
        dirName.setText(FOLDER_PATH);

        if (command.equals("locker")) {
            if (!locked) {
                JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                row2.add(labelNewPassword);
                row2.add(passFieldNew);
                frame.add(row2);

                JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                row3.add(labelConfirmPassword);
                row3.add(passFieldConfirm);
                frame.add(row3);

                JPanel row4 = new JPanel(new FlowLayout(FlowLayout.CENTER));
                row4.add(lockButton);
                frame.add(row4);
            }

            else {
                JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                row2.add(labelEnterPassword);
                row2.add(passFieldEnter);
                frame.add(row2);

                JPanel row3 = new JPanel(new FlowLayout(FlowLayout.CENTER));
                row3.add(new JLabel());
                frame.add(row3);

                JPanel row4 = new JPanel(new FlowLayout(FlowLayout.CENTER));
                row4.add(unlockButton);
                frame.add(row4);
            }
        } else {
            JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
            row2.add(infoMessage);
            frame.add(row2);

            JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row3.add(labelEnterPassword);
            row3.add(passFieldEnter);
            frame.add(row3);

            JPanel row4 = new JPanel(new FlowLayout(FlowLayout.CENTER));
            row4.add(removeButton);
            frame.add(row4);
        }

        JPanel row5 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        row5.add(new JLabel());
        row5.add(responseMessage);
        row5.add(new JLabel());
        frame.add(row5);

        frame.setVisible(true);
    }

    public void fieldResponse() {
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                colorBorder();
            }

            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                colorBorder();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                colorBorder();
            }

            private void colorBorder() {
                responseMessage.setText(null);
                passFieldNew.setBorder(new JTextField().getBorder());
                passFieldConfirm.setBorder(new JTextField().getBorder());
                passFieldEnter.setBorder(new JTextField().getBorder());
            }
        };

        passFieldNew.getDocument().addDocumentListener(documentListener);
        passFieldConfirm.getDocument().addDocumentListener(documentListener);
        passFieldEnter.getDocument().addDocumentListener(documentListener);
    }

    private void openFolder(String folderPath) {
        try {
            Process p = Runtime.getRuntime().exec(new String[] {"explorer.exe", "/root,", folderPath});
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void changeFolderIcon(String folderPath, String iconPath, boolean locked) {
        if (locked) {
            File fileToDelete = new File(folderPath + "\\" + "desktop.ini");
            if (fileToDelete.delete()) {
                System.out.println("Icon removed successfully");
            } else {
                System.out.println("Error while removing icon");
            }
        } else {
            try {
                File iniFile = new File(folderPath, "desktop.ini");
                iniFile.createNewFile();

                BufferedWriter writer = new BufferedWriter(new FileWriter(iniFile));
                writer.write("[.ShellClassInfo]");
                writer.newLine();
                writer.write("IconFile=" + iconPath);
                writer.newLine();
                writer.write("IconIndex=0");
                writer.newLine();
                writer.close();

                Files.setAttribute(iniFile.toPath(), "dos:hidden", true);
                Files.setAttribute(new File(folderPath).toPath(), "dos:system", true);

                Files.move(new File(folderPath).toPath(), new File(folderPath + "_temp").toPath());
                Files.move(new File(folderPath + "_temp").toPath(), new File(folderPath).toPath());

                System.out.println("Icon changed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void locker(boolean locked) {
        lockButton.addActionListener((e) -> {
            String val1 = String.valueOf(passFieldNew.getPassword());
            String val2 = String.valueOf(passFieldConfirm.getPassword());

            if (val1.length() == 0 && val2.length() == 0) {
                responseMessage.setText("All Fields Must Be Filled");
                passFieldNew.setBorder(BorderFactory.createLineBorder(Color.RED));
                passFieldConfirm.setBorder(BorderFactory.createLineBorder(Color.RED));
            } else if (val1.length() == 0) {
                responseMessage.setText("All Fields Must Be Filled");
                passFieldNew.setBorder(BorderFactory.createLineBorder(Color.RED));
            } else if (val2.length() == 0) {
                responseMessage.setText("All Fields Must Be Filled");
                passFieldConfirm.setBorder(BorderFactory.createLineBorder(Color.RED));
            } else if (val1.equals(val2)) {
                SecretKey secretKey = handler.generateKey();
                handler.encrypt(secretKey);
                this.storePassword(val1, handler.keyToString(secretKey));
                responseMessage.setText("Password Stored Successfully");
                this.changeFolderIcon(FOLDER_PATH, LOCKED_ICON_PATH, false);
                System.exit(0);
            } else {
                responseMessage.setText("Password Doesn't Match");
            }
        });

        unlockButton.addActionListener((e) -> {
            String[] passwordData = extractPassword();

            assert passwordData != null;
            if (Objects.equals(passwordData[0], encrypt(String.valueOf(passFieldEnter.getPassword())))) {
                responseMessage.setText("Password Matched");
                SecretKey secretKey = handler.extractKey(passwordData[1]);
                handler.decrypt(secretKey);
                openFolder(FOLDER_PATH);
                System.exit(0);
            } else {
                responseMessage.setText("Password Doesn't Match");
            }
        });

        removeButton.addActionListener((e) -> {
            String[] passwordData = extractPassword();

            assert passwordData != null;
            if (Objects.equals(passwordData[0], encrypt(String.valueOf(passFieldEnter.getPassword())))) {
                try {
                    this.changeFolderIcon(FOLDER_PATH, LOCKED_ICON_PATH, true);
                    this.removePassword();
                    responseMessage.setText("Password Removed Successfully");
                    System.exit(0);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                responseMessage.setText("Password Doesn't Match");
            }
        });
    }
}
