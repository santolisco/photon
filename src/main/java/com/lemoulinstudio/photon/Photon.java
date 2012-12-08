package com.lemoulinstudio.photon;

import com.lemoulinstudio.photon.entity.PhotonFile;
import com.lemoulinstudio.photon.util.FileTree;
import com.lemoulinstudio.photon.util.filter.MediaFileFilter;
import com.mongodb.Mongo;
import java.io.File;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

public class Photon {

  private final MongoTemplate db;

  public Photon() throws UnknownHostException {
    this.db = new MongoTemplate(new Mongo(), "photon");
  }

  public void importFiles(File importDirectory) {
    long nbFiles = 0;
    long totalFileSize = 0;

    FileTree fileTree = new FileTree(importDirectory, new MediaFileFilter());
    
    DateTimeFormatter dateTimeFormatter = DateTimeFormat
            .forPattern("yyyy-MM-dd HH:mm")
            .withZone(DateTimeZone.forOffsetHours(8));

    for (File file : fileTree) {
      try {
        PhotonFile photonFile = new PhotonFile(file);
        db.save(photonFile);

        nbFiles++;
        totalFileSize += photonFile.getLength();
        System.out.println(String.format("[%s] [%s] %s",
                dateTimeFormatter.print(photonFile.getDate().getTime()),
                photonFile.getChecksum(),
                photonFile.getPath()));
      } catch (Exception ex) {
        Logger.getLogger(Photon.class.getName()).log(Level.SEVERE, file.getAbsolutePath(), ex);
      }
    }

    System.out.println(String.format(
            "Imported %d file(s), with a total size of %d byte(s).",
            nbFiles, totalFileSize));
  }
  
  public void exportSorted(File exportDirectory) {
    DateTimeFormatter dateTimeFormatter = DateTimeFormat
            .forPattern("yyyy-MM-dd HH:mm")
            .withZone(DateTimeZone.forOffsetHours(8));
    
    Query query = new Query();
    
    long count = db.count(query, PhotonFile.class);
    final int pageSize = 100;
    for (int page = 0; page * pageSize < count; page++) {
      List<PhotonFile> photonFiles = db.find(query.with(new PageRequest(page, pageSize, new Sort("date", "name"))), PhotonFile.class);
      for (PhotonFile photonFile : photonFiles) {
        System.out.println(String.format("[%s] [%s] %s",
                dateTimeFormatter.print(photonFile.getDate().getTime()),
                photonFile.getChecksum(),
                photonFile.getPath()));
      }
    }
    
  }

  public void closeDB() {
    db.getDb().getMongo().close();
  }

  public static void main(String[] args) throws UnknownHostException {
    Photon photon = new Photon();

    //photon.importFiles(new File(args[0]));
    
    photon.exportSorted(new File(args[0]));

    photon.closeDB();
  }
}
