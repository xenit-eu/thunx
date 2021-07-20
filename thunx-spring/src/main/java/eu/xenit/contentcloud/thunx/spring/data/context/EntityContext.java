package eu.xenit.contentcloud.thunx.spring.data.context;

import org.springframework.data.repository.core.EntityInformation;

public class EntityContext {

    private static ThreadLocal<EntityInformation> currentEntityContext = new InheritableThreadLocal<EntityInformation>();

    public static EntityInformation getCurrentEntityContext() {
        return currentEntityContext.get();
    }

    public static void setCurrentEntityContext(EntityInformation ei) {
        currentEntityContext.set(ei);
    }

    public static void clear() {
        currentEntityContext.set(null);
    }
}
