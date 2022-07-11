package client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Client {
    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 7777;
    private static final String PATH_TO_CLIENT_FILES = "C:\\Users\\dante\\IdeaProjects\\File Server1\\File Server\\task\\src\\client\\data\\";
    private static Socket socket;

    public static void main(String[] args) {

        /*
        I originally had all the code in one big switch statement with nested switches. I decided to decompose it all
        into separate methods. This makes it more readable at a glance, and you can go to any specific method if you
        want, and it also frees up namespace. The only thing is we are creating new sockets and new streams
        */

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            socket = new Socket(ADDRESS, PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter action (1 - get a file, 2 - save a file, 3 - delete a file): ");
        switch (sc.next()) {
            case "1":
                get();
                break;
            case "2":
                put();
                break;
            case "3":
                delete();
                break;
            case "exit":
                exit();
                break;
            default:
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                throw new IllegalStateException("Wrong Number!");
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void get() {
        Scanner sc = new Scanner(System.in);
        byte[] content;
        int lengthOfContent;
        try (
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
        ) {
            System.out.print("Do you want to get the file by name or by id (1 - name, 2 - id): ");
            switch (sc.next()) {
                case "1":
                    System.out.print("Enter filename: ");
                    String serverName = sc.next();
                    dos.writeUTF("GET");
                    dos.writeUTF("BY_NAME");
                    dos.writeUTF(serverName);
                    System.out.println("The request was sent.");
                    break;
                case "2":
                    System.out.print("Enter id: ");
                    int id = sc.nextInt();
                    dos.writeUTF("GET");
                    dos.writeUTF("BY_ID");
                    dos.writeInt(id);
                    System.out.println("The request was sent.");
                    break;
                default:
                    throw new IllegalStateException("Wrong Number!");
            }
            String responseCode = dis.readUTF();
            switch (responseCode) {
                case "200":     // Carry on with the rest of the code. Is this good practice? personally IDK.
                    break;
                case "404":
                    System.out.println("The response says that this file is not found!\n");
                    return;
                default:
                    throw new IllegalStateException("Unexpected value: " + responseCode);
            }
            lengthOfContent = dis.readInt();
            content = new byte[lengthOfContent];
            dis.readFully(content);

            System.out.print("The file was downloaded! Specify a name for it: ");
            String localName = sc.next();
            Files.write(Path.of(PATH_TO_CLIENT_FILES + localName), content);
            System.out.println("File saved on the hard drive!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void put() {
        Scanner sc = new Scanner(System.in);
        try (
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
        ) {
            System.out.print("Enter name of the file: ");
            String path = PATH_TO_CLIENT_FILES + sc.nextLine();
            System.out.print("Enter name of the file to be saved on server: ");
            String serverName = sc.nextLine();

            byte[] content = Files.readAllBytes(Path.of(path));

            dos.writeUTF("PUT");
            dos.writeUTF(serverName);
            dos.writeInt(content.length);
            dos.write(content);

            System.out.println("The request was sent.");
            String responseCode = dis.readUTF();
            switch (responseCode) {
                case "200":
                    System.out.println("Response says that file is saved! ID = " + dis.readInt() + "\n");
                    return;
                case "403":
                    System.out.println("The response says that creating the file was forbidden!\n");
                    return;
                default:
                    throw new IllegalStateException("Unexpected value: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void delete() {
        Scanner sc = new Scanner(System.in);
        try (
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
        ) {
            System.out.print("Do you want to delete the file by name or by id (1 - name, 2 - id): ");
            switch (sc.next()) {
                case "1":
                    System.out.print("Enter filename: ");
                    String serverName = sc.next();

                    dos.writeUTF("DELETE");
                    dos.writeUTF("BY_NAME");
                    dos.writeUTF(serverName);
                    break;
                case "2":
                    System.out.print("Enter id: ");
                    int id = sc.nextInt();

                    dos.writeUTF("DELETE");
                    dos.writeUTF("BY_ID");
                    dos.writeInt(id);
                    break;
                default:
                    throw new IllegalStateException("Wrong Number!");
            }
            System.out.println("The request was sent.");
            String responseCode = dis.readUTF();
            switch (responseCode) {
                case "200":
                    System.out.println("The response says that the file was successfully deleted!\n");
                    break;
                case "404":
                    System.out.println("The response says that this file is not found!\n");
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void exit() {
        try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            dos.writeUTF("EXIT");
            System.out.println("The request was sent.\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
