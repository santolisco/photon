package com.lemoulinstudio.photon.util;

import com.lemoulinstudio.photon.util.filter.YesFileFilter;
import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;

public class FileTree implements Iterable<File> {
  private final File root;
  private final FileFilter filter;

  public FileTree(File root) {
    this(root, new YesFileFilter());
  }

  public FileTree(File root, FileFilter filter) {
    this.root = root;
    this.filter = filter;
  }

  @Override
  public Iterator<File> iterator() {
    return new FileIterator(root, filter);
  }
  
}
