/*
 * Copyright (C) CoSMo Software Consulting Pte. Ltd. - All Rights Reserved
 */

package org.webrtc.kite.config.media;

import io.cosmosoftware.kite.config.KiteEntity;
import io.cosmosoftware.kite.interfaces.JsonBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: MediaFile.
 */

@Entity (name = MediaFile.TABLE_NAME)
public class MediaFile extends KiteEntity implements JsonBuilder {
  
  /** The Constant TABLE_NAME. */
  final static String TABLE_NAME = "mediafiles";
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The id. */
  private String id;
  
  /** The path name name. */
  private String filename;
  
  /** The file name version. */
  private String name;
  
  /** The type of the media. */
  private MediaFileType type;
  
  /** The duration of the media. */
  private String duration = "00:00:00";



  /** The directory with the Videos and Audios folders are located. */
  private String directory = "/home/ubuntu/";
  
  /**
   * Instantiates a new media file.
   */
  public MediaFile() {
    super();
  }
  
  /**
   * Instantiates a new mediaFile.
   *
   * @param jsonObject the json object
   */
  public MediaFile(JsonObject jsonObject) {
    this();    
    // Mandatory
    this.filename = jsonObject.getString("filename");
    this.type = MediaFileType.valueOf(jsonObject.getString("type"));
    
    // Optional
    this.directory = jsonObject.getString("directory", directory);
    this.name = jsonObject.getString("name", this.filename);
    this.duration = jsonObject.getString("duration", duration);
  }
  
  /**
   * Gets the id.
   *
   * @return the id
   */
  @Id
  // @GeneratedValue(generator = "uuid")
  // @GenericGenerator(name = "uuid", strategy = "uuid2")
  @GeneratedValue (generator = MediaFile.TABLE_NAME)
  @GenericGenerator (name = MediaFile.TABLE_NAME, strategy = "io.cosmosoftware.kite.dao.KiteIdGenerator", parameters = {
    @Parameter (name = "prefix", value = "MEDI")
  })
  public String getId() {
    return this.id;
  }
  
  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
  }
  
  /**
   * Gets the filename.
   *
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }


  /**
   * Gets the filepath (directory + filename)
   *
   * @return the filepath
   */
  @Transient
  public String getFilepath() {
    return directory + filename;
  }
  
  /**
   * Sets the filename.
   *
   * @param filename the new filename
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }
  
  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }
  
  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * Gets the type.
   *
   * @return the type
   */
  @Enumerated (EnumType.STRING)
  public MediaFileType getType() {
    return type;
  }
  
  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(MediaFileType type) {
    this.type = type;
  }
  
  /**
   * Gets the duration.
   *
   * @return the duration
   */
  public String getDuration() {
    return duration;
  }
  
  /**
   * Sets the duration.
   *
   * @param duration the new duration
   */
  public void setDuration(String duration) {
    this.duration = duration;
  }
  
  /*
   * (non-Javadoc)
   *
   * @see com.cosmo.kite.interfaces.JsonBuilder#buildJsonObjectBuilder()
   */
  @Override
  public JsonObjectBuilder buildJsonObjectBuilder() {
    return Json.createObjectBuilder().add("name", this.name).add("filename", this.filename)
      .add("type", this.type.name()).add("duration", this.duration).add("directory", this.directory);
  }
  
}
