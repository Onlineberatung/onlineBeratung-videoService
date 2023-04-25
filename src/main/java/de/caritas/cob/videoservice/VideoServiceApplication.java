package de.caritas.cob.videoservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Starter class for the application. */
@SpringBootApplication
public class VideoServiceApplication {

  /**
   * Global application entry point.
   *
   * @param args possible provided args
   */
  public static void main(String[] args) {
    SpringApplication.run(VideoServiceApplication.class, args);
  }
}
