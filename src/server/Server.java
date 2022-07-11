package server;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 7777;
    private static final String PATH_TO_SERVER_FILES = "C:\\Users\\dante\\IdeaProjects\\File Server1\\File Server\\task\\src\\server\\data\\";
    static Map<Integer, File> fileMap;
    private static final File startupInfo = new File("C:\\Users\\dante\\IdeaProjects\\File Server1\\File Server\\task\\src\\server\\meta data\\Startup Info.txt");
    private static final File serializedFileMap = new File("C:\\Users\\dante\\IdeaProjects\\File Server1\\File Server\\task\\src\\server\\meta data\\fileMap.txt");
    private static final ExecutorService sessionExecutor = Executors.newFixedThreadPool(4);
    private static boolean controlFlag = true;

    public static void main(String[] args) {
        System.out.println("Server started!\n");

        try (
                FileReader reader = new FileReader(startupInfo);
                Scanner sc = new Scanner(reader)
        ) {
            IDGenerator.init(sc.nextInt());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            IDGenerator.init(0);
        }

        try (
                FileInputStream fis = new FileInputStream(serializedFileMap);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream ois =  new ObjectInputStream(bis)
        ) {
            fileMap = (Map<Integer, File>) ois.readObject();
        } catch (EOFException e) {
            fileMap = new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (ServerSocket server = new ServerSocket(PORT)) {
            server.setSoTimeout(500);

            while (controlFlag) {
                try {
                    Session session = new Session(server.accept());
                    session.start();
                } catch (SocketTimeoutException ignored) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static synchronized int getID(File file) {
        for (var e : fileMap.entrySet()) {
            if (e.equals(file)) {
                return e.getKey();
            }
        }
        return -1;
    }

    static synchronized void get(Socket socket) {
        try (
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
        ) {
            byte[] content;
            switch(dis.readUTF()) {
                case "BY_ID":
                    int id = dis.readInt();
                    if (fileMap.containsKey(id)) {
                        content = Files.readAllBytes(Path.of(fileMap.get(id).getPath()));
                    } else {
                        dos.writeUTF("404");
                        return;
                    }
                    break;
                case "BY_NAME":
                    String path = PATH_TO_SERVER_FILES + dis.readUTF();
                    if (fileMap.containsValue(new File(path))) {
                        content = Files.readAllBytes(Path.of(path));
                    } else {
                        dos.writeUTF("404");
                        return;
                    }
                    break;
                default:
                    throw new IllegalStateException("Request Is Wrong!");
            }
            dos.writeUTF("200");
            dos.writeInt(content.length);
            dos.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static synchronized void put(Socket socket) {
        try (
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
        ) {
            int id = IDGenerator.next();
            String name = dis.readUTF();
            String path = name.equals("") ? (PATH_TO_SERVER_FILES + "unnamed" + id) : (PATH_TO_SERVER_FILES + name);
            int lengthOfContent = dis.readInt();
            final byte[] content = new byte[lengthOfContent];
            dis.readFully(content);

            File workingFile = new File(path);
            if (!fileMap.containsValue(workingFile)) {
                fileMap.put(id, workingFile);

                Files.write(Path.of(path), content);
                dos.writeUTF("200");
                dos.writeInt(id);
            } else {
                dos.writeUTF("403");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static synchronized void delete(Socket socket) {
        try (
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
        ) {
            File workingFile;
            switch (dis.readUTF()) {
                case "BY_NAME":
                    String path = PATH_TO_SERVER_FILES + dis.readUTF();
                    workingFile = new File(path);

                    if (workingFile.delete()) {
                        fileMap.remove(getID(workingFile));
                        dos.writeUTF("200");
                    } else {
                        dos.writeUTF("404");
                    }
                    break;
                case "BY_ID":
                    int id = dis.readInt();
                    workingFile = fileMap.get(id);

                    if (workingFile.delete()) {
                        fileMap.remove(id);
                        dos.writeUTF("200");
                    } else {
                        dos.writeUTF("404");
                    }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void exit() {
        System.out.println("Hello");
        controlFlag = false;
        sessionExecutor.shutdown();     // I'm not sure if this is necessary or not.
        try (
                FileOutputStream fos = new FileOutputStream(serializedFileMap);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ObjectOutputStream oos =  new ObjectOutputStream(bos);
                FileWriter fw = new FileWriter(startupInfo)
        ) {
            oos.writeObject(fileMap);
            fw.write(IDGenerator.getCurrentID());
        } catch (IOException e) {
            e.printStackTrace();
        }
        controlFlag = false;

    }
}

enum Request {
    GET, PUT, DELETE, EXIT
}

class Session extends Thread {
    private final Socket socket;

    public Session(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            String request = dis.readUTF();
            System.out.println(request);
            switch (Request.valueOf(request)) {
                case GET:
                    Server.get(socket);
                    break;
                case PUT:
                    Server.put(socket);
                    break;
                case DELETE:
                    Server.delete(socket);
                    break;
                case EXIT:
                    Server.exit();
                    break;
                default:
                    throw new IllegalStateException("Unknown request: " + request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*
 This whole class looks awfully lot like an object on its own. At first, I thought it would be a good idea, to have
 a class devoted to ID management in case we wanted to change the implementation, but now I am questioning whether
 it is a good idea. I wonder if an anonymous class would be a good idea.
 I leave this here mainly for my future self to read and make a wiser decision.
 */

final class IDGenerator {

    private static int currentID;

    // Util class should be uninstantiable.
    private IDGenerator() {}

    public static int getCurrentID() {
        return currentID;
    }

    public static int next() {
        currentID++;
        if (Server.fileMap.containsKey(currentID)) {
            currentID++;
            return next();
        }
        return currentID;
    }

    public static void init(int currentID) {
        IDGenerator.currentID = currentID;
    }
}