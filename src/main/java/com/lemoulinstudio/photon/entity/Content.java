package com.lemoulinstudio.photon.entity;

import com.lemoulinstudio.photon.util.FileUtil;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="content")
public class Content {
  
  @Id
  private String hash; // SHA-256 hash.
  
  @Indexed
  private long length;
  
  private Set<Reference> references;

  @Indexed
  private Set<String> tags;
  
  public Content() {
    this.references = new HashSet<Reference>();
    this.tags = new HashSet<String>();
  }

  public Content(File file) throws Exception {
    this.hash = FileUtil.getSha256(file);
    this.length = file.length();
    this.references = new HashSet<Reference>();
    this.tags = new HashSet<String>();
    
    references.add(new Reference(file));
  }

  public String getHash() {
    return hash;
  }

  public long getLength() {
    return length;
  }

  public Set<Reference> getReferences() {
    return references;
  }

  public Set<String> getTags() {
    return tags;
  }
  
  public Reference getEarliestReference() {
    Iterator<Reference> iterator = references.iterator();
    
    if (!iterator.hasNext()) {
      return null;
    }
    
    Reference result = iterator.next();
    
    while (iterator.hasNext()) {
      Reference reference = iterator.next();
      if (reference.getDate().before(result.getDate())) {
        result = reference;
      }
    }
    
    return result;
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
