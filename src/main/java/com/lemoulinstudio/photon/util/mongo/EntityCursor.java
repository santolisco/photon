package com.lemoulinstudio.photon.util.mongo;

import com.mongodb.DBCursor;
import java.util.Iterator;
import org.springframework.data.mongodb.core.convert.MongoConverter;

public class EntityCursor<T> implements Iterable<T>, Iterator<T> {
  
  private MongoConverter converter;
  private Class<T> entityClass;
  private DBCursor cursor;

  public EntityCursor(MongoConverter converter, Class<T> entityClass, DBCursor cursor) {
    this.converter = converter;
    this.entityClass = entityClass;
    this.cursor = cursor;
  }

  @Override
  public Iterator<T> iterator() {
    return this;
  }

  @Override
  public boolean hasNext() {
    return cursor.hasNext();
  }

  @Override
  public T next() {
    return converter.read(entityClass, cursor.next());
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Not supported.");
  }
  
}
