package com.lemoulinstudio.photon.util;

import com.lemoulinstudio.photon.util.filter.DirectoryOrChainFileFilter;
import com.lemoulinstudio.photon.util.filter.YesFileFilter;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileIterator implements Iterator<File> {

  private static class DirectoryIterationContext {

    public File[] files;
    public int index;

    public DirectoryIterationContext(File[] files) {
      this.files = (files == null ? new File[0] : files);
      this.index = 0;
    }
  }
  
  private final FileFilter filter;
  private final List<DirectoryIterationContext> stack;
  
  private boolean nextHasBeenFetched;
  private File next;

  public FileIterator(File root) {
    this(root, new YesFileFilter());
  }
  
  public FileIterator(File root, FileFilter filter) {
    this.filter = new DirectoryOrChainFileFilter(filter);
    this.stack = new ArrayList<DirectoryIterationContext>();
    stack.add(new DirectoryIterationContext(root.listFiles(this.filter)));
  }

  private File fetchNextFile() {
    while (true) {
      int stackIndex = stack.size() - 1;

      if (stackIndex == -1) {
        return null;
      }

      DirectoryIterationContext ic = stack.get(stackIndex);
      
      if (ic.index >= ic.files.length) {
        stack.remove(stackIndex);
      } else {
        File file = ic.files[ic.index];
        ic.index++;

        if (file.isDirectory()) {
          stack.add(new DirectoryIterationContext(file.listFiles(filter)));
        } else {
          return file;
        }
      }
    }
  }

  private File getNext() {
    if (!nextHasBeenFetched) {
      next = fetchNextFile();
      nextHasBeenFetched = true;
    }

    return next;
  }

  @Override
  public boolean hasNext() {
    return getNext() != null;
  }

  @Override
  public File next() {
    File result = getNext();
    nextHasBeenFetched = false;
    return result;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
