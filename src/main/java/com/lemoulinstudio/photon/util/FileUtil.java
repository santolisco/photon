package com.lemoulinstudio.photon.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.security.MessageDigest;

public class FileUtil {

  public static String byteArrayToHex(byte[] b) {
    StringBuilder sb = new StringBuilder();
    
    for (int i = 0; i < b.length; i++) {
      sb.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
    }
    
    return sb.toString();
  }
  
  private static byte[] buffer = new byte[1024 * 1024];
  
  // Warning: not thread-safe.
  public static String getSha256(File file) throws Exception  {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    FileInputStream fis = new FileInputStream(file);
    
    while (true) {
      int nb = fis.read(buffer);
      if (nb == -1) {
        break;
      }
      md.update(buffer, 0, nb);
    }
    
    fis.close();
    
    return byteArrayToHex(md.digest());
  }
  
  public static void safeCopyFile(File sourceFile, File destFile) throws IOException {
    if (destFile.exists()) {
      throw new IOException("File already exists.");
    }
    
    destFile.getParentFile().mkdirs();
    destFile.createNewFile();

    FileChannel source = null;
    FileChannel destination = null;
    try {
      source = new FileInputStream(sourceFile).getChannel();
      destination = new FileOutputStream(destFile).getChannel();

      // previous code: destination.transferFrom(source, 0, source.size());
      // to avoid infinite loops, should be:
      long size = source.size();
      long count = 0;
      while (count < size) {
        count += destination.transferFrom(source, count, size - count);
      }
    }
    finally {
      if (source != null) {
        source.close();
      }
      if (destination != null) {
        destination.close();
      }
    }
  }
  
  public static void createSymbolicLink(File file, File link) throws IOException {
    link.getParentFile().mkdirs();
    Files.createSymbolicLink(link.toPath(), file.toPath());
  }
  
}
