package com.lemoulinstudio.photon.util.mongo;

import com.mongodb.DBCursor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

public class MongoUtil {
  
  public static <T> Iterable<T> find(MongoTemplate db, Query query, Class<T> entityClass) {
    DBCursor cursor = db.getCollection(db.getCollectionName(entityClass))
            .find(query.getQueryObject(), query.getFieldsObject())
            .sort(query.getSortObject());
    
    return new EntityCursor<T>(db.getConverter(), entityClass, cursor);
  }
  
}
