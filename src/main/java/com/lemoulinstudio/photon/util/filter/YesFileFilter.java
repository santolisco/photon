package com.lemoulinstudio.photon.util.filter;

import java.io.File;
import java.io.FileFilter;

public class YesFileFilter implements FileFilter {

  @Override
  public boolean accept(File file) {
    return true;
  }
  
}
