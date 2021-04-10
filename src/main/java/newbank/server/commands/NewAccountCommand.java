package newbank.server.commands;

import newbank.server.Account;
import newbank.server.CustomerID;
import newbank.server.NewBank;
import newbank.server.exceptions.RequestNotAllowedException;

public class NewAccountCommand extends Command {
  private final NewBank bank;
  private final String[] tokens;
  private final CustomerID customer;

  public NewAccountCommand(final NewBank bank, final String[] tokens, final CustomerID customer) {
    this.bank = bank;
    this.tokens = tokens;
    this.customer = customer;
  }

  protected String getSyntax() {
    return "NEWACCOUNT <Name> [Default]";
  }

  private boolean isFirstNonSavingsAccount(final String accountName) {
    return !Account.isSavingsAccount(accountName) && !bank.hasDefaultAccount(customer);
  }

  @Override
  public String execute() {
    try {
      checkLoggedIn(customer);

      if (!(tokens.length >= 2)) {
        return String.format("FAIL: Usage: %s", getSyntax());
      }

      boolean isDefault = false;
      final String accountName = tokens[1];

      if (tokens.length == 3) {
        if (!tokens[2].equalsIgnoreCase("DEFAULT")) {
          return String.format("FAIL: Usage: %s", getSyntax());
        }

        isDefault = true;
      }

      // first non savings account automatically gets designated as default account
      if (!isDefault && isFirstNonSavingsAccount(accountName)) {
        isDefault = true;
      }

      return bank.newAccount(customer, accountName, isDefault);
    } catch (RequestNotAllowedException ex) {
      return String.format("FAIL: %s", ex.getMessage());
    }
  }
}
