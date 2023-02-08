package work.lclpnet.plugin.load;

import java.util.Optional;

public interface ClassResolver {

    Optional<Class<?>> resolve(String name, ClassLoader delegate);
}
