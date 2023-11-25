package com.danielvm.destiny2bot.context;

public class UserIdentityContext {

  private static final ThreadLocal<UserIdentity> IDENTITY = new ThreadLocal<>();

  public static UserIdentity getUserIdentity() {
    return IDENTITY.get();
  }

  public static void setUserIdentity(UserIdentity identity) {
    IDENTITY.set(identity);
  }

  public static void clearUserIdentity() {
    IDENTITY.remove();
  }
}
