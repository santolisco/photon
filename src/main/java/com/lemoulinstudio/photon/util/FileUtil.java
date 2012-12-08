package com.lemoulinstudio.photon.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class FileUtil {

  private static byte[] buffer = new byte[1024 * 1024];
  
  // Warning: not thread-safe.
  public static String getSha256(File file) throws Exception  {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    FileInputStream fis = new FileInputStream(file);
    
    while (true) {
      int nb = fis.read(buffer);
      if (nb == -1) break;
      md.update(buffer);
    }
    
    fis.close();
    
    return byteArrayToHex(md.digest());
  }

  public static String byteArrayToHex(byte[] b) {
    StringBuilder sb = new StringBuilder();
    
    for (int i = 0; i < b.length; i++) {
      sb.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
    }
    
    return sb.toString();
  }
  
}
