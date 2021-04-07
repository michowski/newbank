package newbank.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import newbank.utils.ConsoleDisplay;
import newbank.utils.Display;

public class ExampleClient extends Thread {

  public static final int DEFAULT_PORT = 14002;
  public static final String DEFAULT_IP = "localhost";
  private final Socket server;
  private final PrintWriter bankServerOut;
  private final BufferedReader userInput;
  private final Thread bankServerResponseThread;
  private Display display = new ConsoleDisplay();

  /**
   * @param ip an ip address, or the loopback address
   * @param port the port that the newbank server will be listening on
   * @throws ConfigurationException af the server cannot be reached at the supplied ip and port
   * @throws IOException if an I/O exception occurs while trying to connect to the server
   */
  public ExampleClient(String ip, int port, Reader reader)
      throws IOException, IllegalThreadStateException, ConfigurationException {
    server = getSocket(ip, port);
    userInput = new BufferedReader(reader);
    bankServerOut = new PrintWriter(server.getOutputStream(), true);

    bankServerResponseThread =
        new Thread() {
          private final BufferedReader bankServerIn =
              new BufferedReader(new InputStreamReader(server.getInputStream()));

          public void run() {
            while (true) {
              String response = null;
              try {
                response = bankServerIn.readLine();
              } catch (IOException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
              }
              if (response == null) {
                break; // socket broken, end thread
              }

              display.writeLine(response);
            }
          }
        };
    bankServerResponseThread.start();
  }

  public void setDisplay(Display display) {
    this.display = display;
  }

  /**
   * Tries to open a socket, but does some error-diagnosis if that fails
   *
   * @param ip an ip address, or the loopback address
   * @param port the port that the newbank server will be listening on
   * @return a socket connected to the newbank server
   * @throws ConfigurationException if the server cannot be reached at the supplied ip and port
   * @throws IOException if an I/O exception occurs while creating the socket
   */
  private Socket getSocket(String ip, int port) throws ConfigurationException, IOException {
    try {
      return new Socket(ip, port);
    } catch (UnknownHostException | SocketException | IllegalArgumentException e) {
      throw new ConfigurationException(
          String.format(
              "The newbank server cannot be reached at ip %s, port number %d.",
              DEFAULT_IP, DEFAULT_PORT));
    }
  }

  public void run() {
    try {
      String command;
      while ((command = userInput.readLine()) != null) {
        bankServerOut.println(command);

        if (command.equals("QUIT")) {
          break; // terminate application
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    try {
      new ExampleClient(DEFAULT_IP, DEFAULT_PORT, new InputStreamReader(System.in)).start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
