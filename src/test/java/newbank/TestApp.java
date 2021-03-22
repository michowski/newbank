package newbank;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import newbank.client.ExampleClient;
import newbank.server.NewBankServer;
import newbank.utils.Display;
import newbank.utils.QueueDisplay;

public class TestApp {
  private static NewBankServer server;

  private PipedReader reader;
  private PipedWriter writer;
  private Display display;
  private ExampleClient client;

  @Ignore
  private String testCommand(String command) throws IOException {
    writer.write(command);
    display.discardLinesUntil("request");

    return display.getLine();
  }

  @BeforeClass
  public static void beforeAll()  throws IOException, InterruptedException {
    server = new NewBankServer(NewBankServer.DEFAULT_SERVER_PORT);
    server.start();
  }

  @Before
  public void setup() throws IOException, InterruptedException {
    reader = new PipedReader();
    writer = new PipedWriter(reader);
    display = new QueueDisplay();

    client = new ExampleClient("localhost", NewBankServer.DEFAULT_SERVER_PORT, reader);
    client.setDisplay(display);
    client.start();
  }

  @After
  public void tearDown() throws IOException, InterruptedException {
    writer.close();
    client.interrupt();
  }

  @Test
  public void canDisplayBalance() throws IOException {
    display.discardLinesUntil("Username");
    writer.write("Bhagy\n");
    display.discardLinesUntil("Password");
    writer.write("bhagy\n");
    display.discardLinesUntil("Successful");

    String result = testCommand("SHOWMYACCOUNTS\n");

    String[] output = result.split(":");
    assertThat(output[0].trim(), equalTo("Main"));
    assertThat(output[1].trim(), equalTo("1000.0"));
  }

  @Test
  public void canCreateNewAccount() throws IOException {
    display.discardLinesUntil("Username");
    writer.write("John\n");
    display.discardLinesUntil("Password");
    writer.write("john\n");
    display.discardLinesUntil("Successful");

    String response;

    response = testCommand("NEWACCOUNT\n");
    assertThat(response, equalTo("FAIL: The proper syntax is: NEWACCOUNT <Name>"));

    response = testCommand("NEWACCOUNT abc\n");
    assertThat(response, equalTo("FAIL: Invalid account name: Length must be between 4 and 12 characters."));

    response = testCommand("NEWACCOUNT ArkadiuszMichowski\n");
    assertThat(response, equalTo("FAIL: Invalid account name: Length must be between 4 and 12 characters."));

    response = testCommand("NEWACCOUNT 123456\n");
    assertThat(response, equalTo("FAIL: Invalid account name: Only letters are allowed."));

    response = testCommand("NEWACCOUNT accountB\n");
    assertThat(response, equalTo("The account has been created successfully."));

    response = testCommand("NEWACCOUNT accountC\n");
    assertThat(response, equalTo("The account has been created successfully."));
    response = testCommand("NEWACCOUNT accountD\n");
    assertThat(response, equalTo("The account has been created successfully."));
    response = testCommand("NEWACCOUNT accountE\n");
    assertThat(response, equalTo("The account has been created successfully."));

    response = testCommand("NEWACCOUNT accountF\n");
    assertThat(response, equalTo("FAIL: Maximum number of accounts is: 5"));
    response = testCommand("NEWACCOUNT accountG\n");
    assertThat(response, equalTo("FAIL: Maximum number of accounts is: 5"));
  }
}