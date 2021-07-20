package eu.xenit.contentcloud.thunx.spring.data.rest;

import java.util.Iterator;
import java.util.Optional;
import org.atteo.evo.inflector.English;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.util.StringUtils;

public final class RepositoryUtils {

    private RepositoryUtils() {}

    public static RepositoryInformation findRepositoryInformation(Repositories repositories, String repository) {
        RepositoryInformation ri = null;
        Iterator var3 = repositories.iterator();

        while(var3.hasNext()) {
            Class<?> clazz = (Class)var3.next();
            Optional<RepositoryInformation> candidate = repositories.getRepositoryInformationFor(clazz);
            if (candidate.isPresent() && repository.equals(repositoryPath(candidate.get()))) {
                ri = candidate.get();
                break;
            }
        }

        return ri;
    }

    private static String repositoryPath(RepositoryInformation info) {
        Class<?> clazz = info.getRepositoryInterface();
        RepositoryRestResource annotation = AnnotationUtils.findAnnotation(clazz, RepositoryRestResource.class);
        String path = annotation == null ? null : annotation.path().trim();
        path = StringUtils.hasText(path) ? path : English.plural(StringUtils.uncapitalize(info.getDomainType().getSimpleName()));
        return path;
    }

}
