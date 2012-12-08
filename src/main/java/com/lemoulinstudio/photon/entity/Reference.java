package com.lemoulinstudio.photon.entity;

import java.io.File;
import java.util.Date;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.data.mongodb.core.index.Indexed;

public class Reference {
  
  @Indexed
  private String path; // Full filename, including the path.
  
  @Indexed
  private String directory; // Only the directory.
  
  @Indexed
  private String name; // Only the filename, including the extension.
  
  @Indexed
  private String extension; // Only the extension after the '.'.
  
  @Indexed
  private Date date; // The creation date.

  public Reference() {
  }

  public Reference(File file) {
    this.path = file.getAbsolutePath();
    this.directory = file.getParentFile().getName();
    this.name = file.getName();
    this.extension = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
    this.date = new Date(file.lastModified());
  }

  public String getPath() {
    return path;
  }

  public String getDirectory() {
    return directory;
  }

  public String getName() {
    return name;
  }

  public String getExtension() {
    return extension;
  }

  public Date getDate() {
    return date;
  }
  
  private static DateTimeFormatter dateTimeFormatter = DateTimeFormat
          .forPattern("yyyy-MM-dd HH:mm")
          .withZone(DateTimeZone.forOffsetHours(8));

  @Override
  public String toString() {
    return String.format("[%s] %s", dateTimeFormatter.print(date.getTime()), path);
  }
  
}
