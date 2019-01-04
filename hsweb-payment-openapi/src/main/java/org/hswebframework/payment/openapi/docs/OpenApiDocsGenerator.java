package org.hswebframework.payment.openapi.docs;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface OpenApiDocsGenerator {

    void generate(List<ApiInfo> apiInfoList, String outputDir);

}
