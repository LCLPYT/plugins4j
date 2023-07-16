package work.lclpnet.testPlugin.spi;

import work.lclpnet.provider.spi.TestService;
import work.lclpnet.provider.spi.TestServiceProvider;

public class TestServiceProviderImpl implements TestServiceProvider {

    @Override
    public TestService create() {
        return new TestServiceImpl();
    }
}
