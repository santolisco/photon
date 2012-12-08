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
              
              ".mpg",
              ".mpeg",
              ".mp4",
              ".mov",
              ".flv",
              
//              ".mp3",
//              ".amr",
            });
  }
}
