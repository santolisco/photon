package com.lemoulinstudio.photon.util.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class RegExFileFilter implements FileFilter {
  
  private final Pattern pattern;

  public RegExFileFilter(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public boolean accept(File file) {
    return pattern.matcher(file.getName()).matches();
  }
  
}
