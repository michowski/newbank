package newbank.server.commands;

import newbank.server.CustomerID;
import newbank.server.exceptions.CommandInvalidSyntaxException;
import newbank.server.exceptions.RequestNotAllowedException;

/** Abstract representation of a command. */
public abstract class Command {
  public abstract String execute() throws CommandInvalidSyntaxException;

  /**
   * @return the command syntax
   */
  public String getSyntax() {
    return "";
  }

  protected void checkLoggedIn(CustomerID customer) throws RequestNotAllowedException {
    if (!isLoggedIn(customer)) {
      throw new RequestNotAllowedException();
    }
  }

  protected boolean isLoggedIn(CustomerID customer) {
    return !customer.getKey().isEmpty();
  }
}
