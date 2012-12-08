package com.lemoulinstudio.photon;

import com.lemoulinstudio.photon.entity.PhotonFile;
import com.lemoulinstudio.photon.util.FileTree;
import com.lemoulinstudio.photon.util.FileUtil;
import com.lemoulinstudio.photon.util.filter.MediaFileFilter;
import com.lemoulinstudio.photon.util.mongo.MongoUtil;
import com.mongodb.Mongo;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

public class Photon {

  private final MongoTemplate db;
  private final DateTimeFormatter dateTimeFormatter;
  private final DateTimeFormatter pathDateFormatter;

  public Photon() throws UnknownHostException {
    this.db = new MongoTemplate(new Mongo(), "photon");
    this.dateTimeFormatter = DateTimeFormat
          .forPattern("yyyy-MM-dd HH:mm")
          .withZone(DateTimeZone.forOffsetHours(8));
    this.pathDateFormatter = DateTimeFormat
          .forPattern("yyyy/MM/dd/")
          .withZone(DateTimeZone.forOffsetHours(8));
  }

  public void importFiles(File importDirectory) {
    long nbFiles = 0;
    long totalFileSize = 0;

    FileTree fileTree = new FileTree(importDirectory, new MediaFileFilter());
    
    for (File file : fileTree) {
      try {
        PhotonFile photonFile = new PhotonFile(file);
        db.save(photonFile);

        nbFiles++;
        totalFileSize += photonFile.getLength();
        printPhotonFile(photonFile);
      } catch (Exception ex) {
        Logger.getLogger(Photon.class.getName()).log(Level.SEVERE, file.getAbsolutePath(), ex);
      }
    }

    System.out.println(String.format(
            "Imported %d file(s), with a total size of %d byte(s).",
            nbFiles, totalFileSize));
  }
  
  public void findDuplicates() {
    PhotonFile prevFile = null;
    boolean prevFilePrinted = false;
      
    for (PhotonFile photonFile : MongoUtil.find(db, new Query()
            .with(new Sort("checksum", "date")), PhotonFile.class)) {
      if ((prevFile != null) && (prevFile.getChecksum().equals(photonFile.getChecksum()))) {
        if (!prevFilePrinted) {
          printPhotonFile(prevFile);
          prevFilePrinted = true;
        }
        printPhotonFile(photonFile);
      }
      else {
        prevFile = photonFile;
        prevFilePrinted = false;
      }
    }
  }
  
  public void exportSorted(File exportDirectory) {
    for (PhotonFile photonFile : MongoUtil.find(db, new Query()
            .with(new Sort("date", "name")), PhotonFile.class)) {
      //printPhotonFile(photonFile);
      File sourceFile = new File(photonFile.getPath());
      File destFile = new File(exportDirectory,
              pathDateFormatter.print(photonFile.getDate().getTime())
              + photonFile.getChecksum() + "_" + photonFile.getName().toLowerCase());
      try {
        FileUtil.safeCopyFile(sourceFile, destFile);
        System.out.println(destFile.getAbsolutePath());
      } catch (IOException ex) {
        Logger.getLogger(Photon.class.getName()).log(Level.SEVERE, photonFile.getPath(), ex);
      }
    }
  }
  
  private void printPhotonFile(PhotonFile photonFile) {
    System.out.println(String.format("[%s] [%s] %s",
            dateTimeFormatter.print(photonFile.getDate().getTime()),
            photonFile.getChecksum(),
            photonFile.getPath()));
  }

  public void closeDB() {
    db.getDb().getMongo().close();
  }

  public static void main(String[] args) throws UnknownHostException {
    Photon photon = new Photon();

    //photon.importFiles(new File(args[0]));
    
    //photon.findDuplicates();
    
    //photon.exportSorted(new File(args[1]));

    photon.closeDB();
  }

}
