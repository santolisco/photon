package com.lemoulinstudio.photon.entity;

import com.lemoulinstudio.photon.util.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

public class PhotonFile {
  
  @Id
  private ObjectId id;
  
  @Indexed(unique=true)
  private String path; // Full filename, including the path.
  
  @Indexed
  private String directory; // Only the directory.
  
  @Indexed
  private String name; // Only the filename, including the extension.
  
  @Indexed
  private String extension; // Only the extension after the '.'.
  
  private long length;
  
  @Indexed
  private String checksum; // The sha256 checksum.

  @Indexed
  private Date date; // The creation date.
  
  @Indexed
  private List<String> tags;

  public PhotonFile() {
  }

  public PhotonFile(File file) throws Exception {
    this.id = new ObjectId();
    this.path = file.getAbsolutePath();
    this.directory = file.getParentFile().getName();
    this.name = file.getName().toLowerCase();
    this.extension = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
    this.length = file.length();
    this.checksum = FileUtil.getSha256(file);
    this.date = new Date(file.lastModified());
    this.tags = new ArrayList<String>();
  }

  public ObjectId getId() {
    return id;
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

  public long getLength() {
    return length;
  }

  public String getChecksum() {
    return checksum;
  }

  public Date getDate() {
    return date;
  }

  public List<String> getTags() {
    return tags;
  }

}
