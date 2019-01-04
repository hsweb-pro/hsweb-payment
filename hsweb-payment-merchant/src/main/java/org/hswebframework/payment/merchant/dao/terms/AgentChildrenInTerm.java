package org.hswebframework.payment.merchant.dao.terms;

import org.hswebframework.payment.api.merchant.AgentMerchantService;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.render.SqlAppender;
import org.hswebframework.ezorm.rdb.render.dialect.term.BoostTermTypeMapper;
import org.hswebframework.web.dao.mybatis.mapper.AbstractSqlTermCustomizer;
import org.hswebframework.web.dao.mybatis.mapper.ChangedTermValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询代理商下代理的数据.
 * where("merchantId$agent",agentId).list();
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Component
public class AgentChildrenInTerm extends AbstractSqlTermCustomizer {

    public static final String term = "agent-children-in";

    @Autowired
    private AgentMerchantService agentMerchantService;

    public AgentChildrenInTerm() {
        super(term);
    }

    @Override
    public SqlAppender accept(String prefix, Term term, RDBColumnMetaData rdbColumnMetaData, String table) {
        ChangedTermValue termValue = this.createChangedTermValue(term);
        List<Object> values = BoostTermTypeMapper.convertList(rdbColumnMetaData, termValue.getOld());
        //重构value
        if (termValue.getOld() == termValue.getValue()) {
            List<Object> self = new ArrayList<>(values);
            values = values.stream()
                    .map(String::valueOf)
                    .map(agentMerchantService::getAllChildrenAgentId)
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
            if (term.getOptions().contains("self")) {
                values.addAll(self);
            }
            termValue.setValue(values);
        }
        List realValues = (List) termValue.getValue();
        SqlAppender appender = new SqlAppender();
        if (realValues.isEmpty()) {
            appender.add(" 1=2 ");
        } else {
            appender.add(createColumnName(rdbColumnMetaData, table));
            appender.add(" ");
            appendCondition(realValues, prefix, appender);
        }
        return appender;
    }
}
