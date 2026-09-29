package tacos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import tacos.messaging.OrderMessagingService;
@SpringBootTest
public class TacoCloudApplicationTests {
  @MockBean
    private OrderMessagingService messageService;
  @Test
  public void contextLoads() {
  }

}
