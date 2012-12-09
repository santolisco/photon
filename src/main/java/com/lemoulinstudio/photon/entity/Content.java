package com.lemoulinstudio.photon.entity;

import com.lemoulinstudio.photon.util.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="content")
public class Content {
  
  @Id
  private String hash; // SHA-256 hash.
  
  @Indexed
  private long length;
  
  private List<Reference> references;

  @Indexed
  private List<String> tags;
  
  public Content() {
    this.references = new ArrayList<Reference>();
    this.tags = new ArrayList<String>();
  }

  public Content(File file) throws Exception {
    this.hash = FileUtil.getSha256(file);
    this.length = file.length();
    this.references = new ArrayList<Reference>();
    this.tags = new ArrayList<String>();
    
    references.add(new Reference(file));
  }

  public String getHash() {
    return hash;
  }

  public long getLength() {
    return length;
  }

  public List<Reference> getReferences() {
    return references;
  }

  public List<String> getTags() {
    return tags;
  }
  
  public Reference getEarliestReference() {
    if (references.isEmpty()) {
      return null;
    }
    
    Reference ref = references.get(0);
    
    for (int i = 1; i < references.size(); i++) {
      if (references.get(i).getDate().before(ref.getDate())) {
        ref = references.get(i);
      }
    }
    
    return ref;
  }

  @Override
  public String toString() {
    String s = String.format("[%s]\n", getHash());
    
    for (Reference reference : getReferences()) {
      s += "  " + reference.toString() + "\n";
    }
    
    return s;
  }

}
