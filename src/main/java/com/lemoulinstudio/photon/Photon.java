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
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
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
  
  public void export(
          File exportDirectory,
          boolean useLinks,
          List<String> tags) {
    System.out.println("Export links:");
    
    Criteria criteria = new Criteria();
    
    if (tags.size() > 0) {
      criteria = criteria.and("tags").all(tags);
    }
    
    for (Content content : MongoUtil.find(db, new Query(criteria), Content.class)) {
      for (Reference reference : content.getReferences()) {
        File source = new File(reference.getPath());
        File destination = new File(exportDirectory,
                new File(source.getParentFile(), reference.getName()).getAbsolutePath());
        try {
          if (useLinks) {
            FileUtil.createSymbolicLink(source, destination);
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
          List<String> tags) {
    System.out.println("Export:");
    
    Criteria criteria = new Criteria();
    
    if (tags.size() > 0) {
      criteria = criteria.and("tags").all(tags);
    }
    
    for (Content content : MongoUtil.find(db, new Query(criteria), Content.class)) {
      Reference reference = content.getEarliestReference();
      File source = new File(reference.getPath());
      File destination = new File(exportDirectory,
              pathDateFormatter.print(reference.getDate().getTime()) + reference.getName());
      try {
        if (useLinks) {
          FileUtil.createSymbolicLink(source, destination);
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
  
  public void tagFileTree(File root, List<String> tags) {
    for (File file : new FileTree(root)) {
      try {
        String pointedPath = Files.readSymbolicLink(file.toPath()).toString();
        Content content = db.findOne(new Query(new Criteria("references.path").is(pointedPath)), Content.class);
        content.getTags().addAll(tags);
        db.save(content);
      } catch (IOException ex) {
        Logger.getLogger(Photon.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  public void untagFileTree(File root, List<String> tags) {
    for (File file : new FileTree(root)) {
      try {
        String pointedPath = Files.readSymbolicLink(file.toPath()).toString();
        Content content = db.findOne(new Query(new Criteria("references.path").is(pointedPath)), Content.class);
        content.getTags().removeAll(tags);
        db.save(content);
      } catch (IOException ex) {
        Logger.getLogger(Photon.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  public void closeDB() {
    db.getDb().getMongo().close();
  }

  public static void main(String[] args) throws UnknownHostException {
    Photon photon = new Photon();
    //photon.importFiles(new File(args[0]));
    //photon.findDuplicates();
    //photon.export(new File(args[1]), true, Arrays.<String>asList("pomme"));
    //photon.exportSorted(new File(args[1]), true, Arrays.<String>asList("relevant"));
    //photon.tagFileTree(new File(args[1]), Arrays.<String>asList("pomme"));
    photon.closeDB();
  }

}
