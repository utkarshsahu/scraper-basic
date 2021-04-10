package com.example.webscraper;

public enum CreditsEnum {
  YES(true), NO(false);
  private boolean flag;
  private CreditsEnum(boolean val) {
    flag = val;
  }

  public String getFlag() {
    return Boolean.toString(flag);
  }
}
