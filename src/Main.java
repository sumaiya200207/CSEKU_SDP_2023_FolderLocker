import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Process process = Runtime.getRuntime().exec("runas /user:Administrator cmd.exe");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

         String SOFTWARE_TITLE = "Folder Locker";
         String ICON_FILE_NAME = "icon.png";
         String LOCKED_ICON_FILE_NAME = "C:\\Users\\USER\\Desktop\\Folder3\\lock.ico";
         String PASSWORD_FILE_NAME = "PasswordDirectory.txt";
         String INSTALL_PATH = System.getProperty("InstallationPath");
         String ICON_PATH = INSTALL_PATH != null ? INSTALL_PATH + "/" + ICON_FILE_NAME : ICON_FILE_NAME;
         String LOCKED_ICON_PATH = INSTALL_PATH != null ? INSTALL_PATH + "/" + LOCKED_ICON_FILE_NAME : LOCKED_ICON_FILE_NAME;
         Integer WINDOW_WIDTH = 400;
         Integer WINDOW_HEIGHT = 180;
         String FOLDER_PATH = args[0];
         String COMMAND = args[1];
          //String FOLDER_PATH = "E:\\Test";
          //String COMMAND = "locker";

        FolderLocker folderlocker = new FolderLocker(
                SOFTWARE_TITLE,
                WINDOW_WIDTH,
                WINDOW_HEIGHT,
                INSTALL_PATH,
                FOLDER_PATH,
                PASSWORD_FILE_NAME,
                LOCKED_ICON_PATH
        );

        boolean locked = folderlocker.isLocked();
        folderlocker.fieldResponse();
        folderlocker.addComponents(locked, COMMAND);
        folderlocker.locker(locked);

        Tray tray = new Tray(SOFTWARE_TITLE, ICON_PATH, "Protect your folder from unwanted access");
        tray.showTray();
    }
}