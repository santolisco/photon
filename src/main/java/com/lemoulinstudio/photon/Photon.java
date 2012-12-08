package com.lemoulinstudio.photon;

import com.lemoulinstudio.photon.entity.Content;
import com.lemoulinstudio.photon.entity.Reference;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class Photon {

  private final MongoTemplate db;
  private final DateTimeFormatter pathDateFormatter;

  public Photon() throws UnknownHostException {
    this.db = new MongoTemplate(new Mongo(), "photon");
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
        Content content = new Content(file);
        
        db.upsert(new Query(new Criteria("hash").is(content.getHash())
                .and("length").is(content.getLength())),
                new Update().addToSet("references", content.getReferences().get(0)),
                Content.class);

        nbFiles++;
        totalFileSize += content.getLength();
        System.out.println(content.getReferences().get(0));
      } catch (Exception ex) {
        Logger.getLogger(Photon.class.getName()).log(Level.SEVERE, file.getAbsolutePath(), ex);
      }
    }

    System.out.println(String.format(
            "Imported %d file(s), with a total size of %d byte(s).\n",
            nbFiles, totalFileSize));
  }
  
  public void printDBContent() {
    for (Content content : MongoUtil.find(db, new Query(), Content.class)) {
      System.out.print(content);
    }
  }
  
  public void findDuplicates() {
    System.out.println("Duplicate files:");
    
    for (Content content : MongoUtil.find(db, new Query(
            new Criteria("references").not().size(1)), Content.class)) {
      System.out.print(content);
    }
    
    System.out.println();
  }
  
  public void exportSorted(File exportDirectory) {
    System.out.println("Export:");
    
    for (Content content : MongoUtil.find(db, new Query(), Content.class)) {
      Reference reference = content.getEarliestReference();
      File sourceFile = new File(reference.getPath());
      File destFile = new File(exportDirectory,
              pathDateFormatter.print(reference.getDate().getTime())
              + reference.getName());
      try {
        FileUtil.safeCopyFile(sourceFile, destFile);
        System.out.println(destFile.getAbsolutePath());
      } catch (IOException ex) {
        System.out.println("ERROR, skip: " + destFile.getAbsolutePath());
      }
    }
    
    System.out.println();
  }
  
  public void closeDB() {
    db.getDb().getMongo().close();
  }

  public static void main(String[] args) throws UnknownHostException {
    Photon photon = new Photon();

    photon.importFiles(new File(args[0]));
    
    photon.findDuplicates();
    
    photon.exportSorted(new File(args[1]));

    photon.closeDB();
  }

}
