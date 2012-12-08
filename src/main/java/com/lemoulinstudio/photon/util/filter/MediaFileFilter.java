package com.lemoulinstudio.photon.util.filter;

public class MediaFileFilter extends ExtensionFileFilter {

  public MediaFileFilter() {
    super(new String[]{
      
              ".jpg",
              ".jpeg",
              
              ".png",
              ".bmp",
              ".tiff",
              ".pcx",
              ".gif",
              
              ".avi",
              ".mpg",
              ".mpeg",
              ".mp4",
              ".mov",
              ".flv",
              ".3gp",
              ".mts",
              
//              ".mp3",
//              ".amr",
            });
  }
}
