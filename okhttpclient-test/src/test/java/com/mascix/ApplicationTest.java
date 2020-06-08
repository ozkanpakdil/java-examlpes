
package com.mascix;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import lombok.extern.java.Log;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@Log
@DisplayName("Playing with okhttp")
public class ApplicationTest {

  MockWebServer server;
  CallUrl caller;

  @BeforeEach
  public void setup() throws Exception {
    caller=new CallUrl();
    server = new MockWebServer();
    Dispatcher dispatcher = new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest arg0) throws InterruptedException {
        log.info("Request headers:\n" + arg0.getHeaders());
        return new MockResponse().setBody("hello world");
      }
    };

    server.setDispatcher(dispatcher);
    server.start();
  }

  @AfterEach
  public void closeUp() throws Exception {
    server.close();
  }

  @Test
  @DisplayName("Requesting with special user agent")
  void requestWithSpecialUserAgent() throws Exception {
    log.info(caller.changeUserAgent(server.url("/")));
  }

  @Test
  @DisplayName("Requesting without user agent")
  void requestWithoutUserAgent() throws Exception {
    log.info(caller.removeUserAgent(server.url("/")));
  }
}
