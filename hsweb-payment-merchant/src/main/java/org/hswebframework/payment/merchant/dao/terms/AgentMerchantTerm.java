package org.hswebframework.payment.merchant.dao.terms;

import org.hswebframework.payment.api.merchant.AgentMerchantService;
import org.hswebframework.ezorm.core.MethodReferenceColumn;
import org.hswebframework.ezorm.core.StaticMethodReferenceColumn;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.term.BoostTermTypeMapper;
import org.hswebframework.web.dao.mybatis.mapper.AbstractSqlTermCustomizer;
import org.hswebframework.web.dao.mybatis.mapper.ChangedTermValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询代理商下商户的数据.
 * where("merchantId$agent",agentId).list();
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Component
public class AgentMerchantTerm extends AbstractSqlTermCustomizer {

    public static final String term = "agent";

    @Autowired
    private AgentMerchantService agentMerchantService;

    public AgentMerchantTerm() {
        super(term);
    }

    @Override
    public SqlAppender accept(String prefix, Term term, RDBColumnMetaData rdbColumnMetaData, String table) {
        ChangedTermValue termValue = this.createChangedTermValue(term);
        List<Object> values = BoostTermTypeMapper.convertList(rdbColumnMetaData, termValue.getOld());
        boolean not = term.getOptions().contains("not");
        //重构value
        if (termValue.getOld() == termValue.getValue()) {
            boolean children = term.getOptions().contains("children");
            if (children) {
                values.addAll(values.stream()
                        .map(String::valueOf)
                        .map(agentMerchantService::getAllChildrenAgentId)
                        .flatMap(Collection::stream)
                        .distinct()
                        .collect(Collectors.toList()));
            }
            termValue.setValue(values);
        }
        SqlAppender appender = new SqlAppender();
        appender.add(not ? "not " : "", "exists(select 1 from mer_merchant mer where mer.id = ", createColumnName(rdbColumnMetaData, table));
        appender.add(" and mer.agent_id ");
        appendCondition((List) termValue.getValue(), prefix, appender);
        appender.add(")");
        return appender;
    }
}
