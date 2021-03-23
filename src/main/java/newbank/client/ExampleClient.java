package newbank.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;
import java.net.UnknownHostException;

import newbank.utils.ConsoleDisplay;
import newbank.utils.Display;

public class ExampleClient extends Thread {

  private Socket server;
  private PrintWriter bankServerOut;
  private BufferedReader userInput;
  private Thread bankServerResponseThread;
  private Display display = new ConsoleDisplay();

  public ExampleClient(String ip, int port, Reader reader)
      throws UnknownHostException, IOException {
    server = new Socket(ip, port);
    userInput = new BufferedReader(reader);
    bankServerOut = new PrintWriter(server.getOutputStream(), true);

    bankServerResponseThread =
        new Thread() {
          private BufferedReader bankServerIn =
              new BufferedReader(new InputStreamReader(server.getInputStream()));

          public void run() {
            try {
              while (true) {
                String response = bankServerIn.readLine();

                if (response == null) {
                  // socket broken, end thread
                  return;
                }

                display.writeLine(response);
              }
            } catch (IOException e) {
              e.printStackTrace();
              return;
            }
          }
        };
    bankServerResponseThread.start();
  }

  public void setDisplay(Display display) {
    this.display = display;
  }

  public void run() {
    while (true) {
      try {
        while (true) {
          String command = userInput.readLine();

          if (command == null) {
            // EOF reached, end thread
            return;
          }

          bankServerOut.println(command);
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args)
      throws UnknownHostException, IOException, InterruptedException {
    new ExampleClient("localhost", 14002, new InputStreamReader(System.in)).start();
  }
}
