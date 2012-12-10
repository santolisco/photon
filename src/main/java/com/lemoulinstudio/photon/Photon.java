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
      if (db.findOne(new Query(new Criteria("references.path")
              .is(file.getAbsolutePath())), Content.class) == null) {
        try {
          Content content = new Content(file);
          Reference reference = content.getReferences().iterator().next();

          db.upsert(new Query(new Criteria("hash").is(content.getHash())
                  .and("length").is(content.getLength())),
                  new Update().addToSet("references", reference),
                  Content.class);

          nbFiles++;
          totalFileSize += content.getLength();
          System.out.println(reference);
        } catch (Exception ex) {
          Logger.getLogger(Photon.class.getName()).log(Level.SEVERE, file.getAbsolutePath(), ex);
        }
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
  
  public void export(File exportDirectory, boolean useLinks) {
    System.out.println("Export links:");
    
    for (Content content : MongoUtil.find(db, new Query(), Content.class)) {
      for (Reference reference : content.getReferences()) {
        File source = new File(reference.getPath());
        File destination = new File(exportDirectory,
                new File(source.getParentFile(),
                    content.getHash() + "-" + reference.getName()).getAbsolutePath());
        try {
          if (useLinks) {
            FileUtil.createSymbolicLink(
                    source.getAbsolutePath(),
                    destination.getAbsolutePath());
          }
          else {
            FileUtil.safeCopyFile(source, destination);
          }
          System.out.println(destination.getAbsolutePath());
        } catch (IOException ex) {
          System.out.println("ERROR, skip: " + destination.getAbsolutePath());
        }
      }
    }
  }
  
  public void exportSorted(
          File exportDirectory,
          boolean useLinks,
          String ... tags) {
    System.out.println("Export:");
    
    Criteria criteria = new Criteria();
    
    //if (tags.length > 0) {
    //  criteria = criteria.and("tags").in(tags)
    //}
    
    for (Content content : MongoUtil.find(db, new Query(criteria), Content.class)) {
      Reference reference = content.getEarliestReference();
      File source = new File(reference.getPath());
      File destination = new File(exportDirectory,
              pathDateFormatter.print(reference.getDate().getTime())
              + content.getHash() + "-" + reference.getName());
      try {
        if (useLinks) {
          FileUtil.createSymbolicLink(
                  source.getAbsolutePath(),
                  destination.getAbsolutePath());
        }
        else {
          FileUtil.safeCopyFile(source, destination);
        }
        System.out.println(destination.getAbsolutePath());
      } catch (IOException ex) {
        System.out.println("ERROR, skip: " + destination.getAbsolutePath());
      }
    }
    
    System.out.println();
  }
  
  public void tagFileTree(File root, String tag) {
    for (File file : new FileTree(root)) {
      String name = file.getName();
      String hash = name.substring(0, name.indexOf('-'));
      Content content = db.findById(hash, Content.class);
      content.getTags().add(tag);
      db.save(content);
    }
  }

  public void closeDB() {
    db.getDb().getMongo().close();
  }

  public static void main(String[] args) throws UnknownHostException {
    Photon photon = new Photon();
    //photon.importFiles(new File(args[0]));
    //photon.findDuplicates();
    //photon.export(new File(args[1]), true, "relevant");
    //photon.exportSorted(new File(args[1]), true, "relevant");
    //photon.tagFileTree(new File(args[0]), "relevant");
    photon.closeDB();
  }

}
