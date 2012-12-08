package com.lemoulinstudio.photon.util.filter;

import java.io.File;
import java.io.FileFilter;

public class DirectoryOrChainFileFilter implements FileFilter {
  
  private final FileFilter nextFilter;

  public DirectoryOrChainFileFilter(FileFilter nextFilter) {
    this.nextFilter = nextFilter;
  }

  @Override
  public boolean accept(File file) {
    if (file.isDirectory()) {
      return true;
    }
    else {
      return nextFilter.accept(file);
    }
  }
  
}
