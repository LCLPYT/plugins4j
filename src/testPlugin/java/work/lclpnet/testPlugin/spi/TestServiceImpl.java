package work.lclpnet.testPlugin.spi;

import work.lclpnet.provider.spi.TestService;

public class TestServiceImpl implements TestService {

    @Override
    public void foo() {
        System.out.println("foo");
    }
}
