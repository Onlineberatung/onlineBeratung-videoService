package de.caritas.cob.videoservice.api.controller;

import de.caritas.cob.videoservice.VideoServiceApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = VideoServiceApplication.class)
@AutoConfigureMockMvc(addFilters = false)
public class VideoControllerIT {

  @Autowired
  private MockMvc mockMvc;
}
