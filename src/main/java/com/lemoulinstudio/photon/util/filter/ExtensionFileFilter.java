package com.lemoulinstudio.photon.util.filter;

import java.io.File;
import java.io.FileFilter;

public class ExtensionFileFilter implements FileFilter {
  
  private final String[] extensions;

  public ExtensionFileFilter(String[] extensions) {
    this.extensions = extensions;
  }
  
  @Override
  public boolean accept(File file) {
    String name = file.getName().toLowerCase();
    for (String extension : extensions) {
      if (name.endsWith(extension)) {
        if (!name.startsWith("._")) {
          return true;
        }
      }
    }
    
    return false;
  }
  
}
