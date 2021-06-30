/**
 * Copyright 2020 Tianshu AI Platform. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =============================================================
 */
package org.dubhe.biz.permission.interceptor;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisDefaultParameterHandler;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.parser.ISqlParser;
import com.baomidou.mybatisplus.core.parser.SqlInfo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.handlers.AbstractSqlParserHandler;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectModel;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.IDialect;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import com.baomidou.mybatisplus.extension.toolkit.SqlParserUtils;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.dubhe.biz.base.annotation.DataPermission;
import org.dubhe.biz.base.context.DataContext;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.enums.OperationTypeEnum;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.permission.util.SqlUtil;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description MybatisPlus 分页拦截器
 * @date 2020-11-25
 */
@Intercepts({@Signature(
        type = StatementHandler.class,
        method = "prepare",
        args = {Connection.class, Integer.class}
)})
public class PaginationInterceptor extends AbstractSqlParserHandler implements Interceptor {
    protected static final Log logger = LogFactory.getLog(PaginationInterceptor.class);

    
    @Resource
    private UserContextService userContextService;
    
    /**
     * COUNT SQL 解析
     */
    protected ISqlParser countSqlParser;
    /**
     * 溢出总页数，设置第一页
     */
    protected boolean overflow = false;
    /**
     * 单页限制 500 条，小于 0 如 -1 不受限制
     */
    protected long limit = 500L;
    /**
     * 数据类型
     */
    private DbType dbType;
    /**
     * 方言
     */
    private IDialect dialect;
    /**
     * 方言类型
     */
    @Deprecated
    protected String dialectType;
    /**
     * 方言实现类
     */
    @Deprecated
    protected String dialectClazz;

    public PaginationInterceptor() {
    }

    /**
     * 构建分页sql
     *
     * @param originalSql 原生sql
     * @param page        分页参数
     * @return 构建后 sql
     */
    public static String concatOrderBy(String originalSql, IPage<?> page) {
        if (CollectionUtils.isNotEmpty(page.orders())) {
            try {
                List<OrderItem> orderList = page.orders();
                Select selectStatement = (Select) CCJSqlParserUtil.parse(originalSql);
                List orderByElements;
                List orderByElementsReturn;
                if (selectStatement.getSelectBody() instanceof PlainSelect) {
                    PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();
                    orderByElements = plainSelect.getOrderByElements();
                    orderByElementsReturn = addOrderByElements(orderList, orderByElements);
                    plainSelect.setOrderByElements(orderByElementsReturn);
                    return plainSelect.toString();
                }

                if (selectStatement.getSelectBody() instanceof SetOperationList) {
                    SetOperationList setOperationList = (SetOperationList) selectStatement.getSelectBody();
                    orderByElements = setOperationList.getOrderByElements();
                    orderByElementsReturn = addOrderByElements(orderList, orderByElements);
                    setOperationList.setOrderByElements(orderByElementsReturn);
                    return setOperationList.toString();
                }

                if (selectStatement.getSelectBody() instanceof WithItem) {
                    return originalSql;
                }

                return originalSql;
            } catch (JSQLParserException var7) {
                logger.error("failed to concat orderBy from IPage, exception=", var7);
            }
        }

        return originalSql;
    }

    /**
     * 添加分页排序规则
     *
     * @param orderList         分页规则
     * @param orderByElements   分页排序元素
     * @return 分页规则
     */
    private static List<OrderByElement> addOrderByElements(List<OrderItem> orderList, List<OrderByElement> orderByElements) {
        orderByElements = CollectionUtils.isEmpty(orderByElements) ? new ArrayList(orderList.size()) : orderByElements;
        List<OrderByElement> orderByElementList = orderList.stream().filter((item) -> {
            return StringUtils.isNotBlank(item.getColumn());
        }).map((item) -> {
            OrderByElement element = new OrderByElement();
            element.setExpression(new Column(item.getColumn()));
            element.setAsc(item.isAsc());
            element.setAscDescPresent(true);
            return element;
        }).collect(Collectors.toList());
        ( orderByElements).addAll(orderByElementList);
        return orderByElements;
    }

    /**
     * 执行sql查询逻辑
     *
     * @param invocation mybatis 调用类
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        this.sqlParser(metaObject);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (SqlCommandType.SELECT == mappedStatement.getSqlCommandType() && StatementType.CALLABLE != mappedStatement.getStatementType()) {
            BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
            Object paramObj = boundSql.getParameterObject();
            IPage<?> page = null;
            if (paramObj instanceof IPage) {
                page = (IPage) paramObj;
            } else if (paramObj instanceof Map) {
                Iterator var8 = ((Map) paramObj).values().iterator();

                while (var8.hasNext()) {
                    Object arg = var8.next();
                    if (arg instanceof IPage) {
                        page = (IPage) arg;
                        break;
                    }
                }
            }

            if (null != page && page.getSize() >= 0L) {
                if (this.limit > 0L && this.limit <= page.getSize()) {
                    this.handlerLimit(page);
                }

                String originalSql = boundSql.getSql();

                //注解逻辑判断  添加注解了才拦截
                Class<?> classType = Class.forName(mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf(".")));
                String mName = mappedStatement.getId().substring(mappedStatement.getId().lastIndexOf(".") + 1, mappedStatement.getId().length());


                String sqlCommandType = mappedStatement.getSqlCommandType().toString();
                //获取类注解 获取需要忽略拦截的方法名称
                DataPermission dataAnnotation = classType.getAnnotation(DataPermission.class);
                if (!Objects.isNull(dataAnnotation)) {
                    UserContext curUser = userContextService.getCurUser();
                    String[] ignores = dataAnnotation.ignoresMethod();
                    //校验拦截忽略方法名 忽略新增方法 忽略回调/定时方法
                    if (!((!Objects.isNull(ignores) && Arrays.asList(ignores).contains(mName))
                            || OperationTypeEnum.INSERT.getType().equals(sqlCommandType.toLowerCase())
                            || Objects.isNull(curUser)
                            || (!Objects.isNull(DataContext.get()) && DataContext.get().getType()))


                    ) {
                        originalSql = SqlUtil.buildTargetSql(originalSql, SqlUtil.getResourceIds(curUser), curUser);
                    }
                }

                Connection connection = (Connection) invocation.getArgs()[0];
                if (page.isSearchCount() && !page.isHitCount()) {
                    SqlInfo sqlInfo = SqlParserUtils.getOptimizeCountSql(page.optimizeCountSql(), this.countSqlParser, originalSql);
                    this.queryTotal(sqlInfo.getSql(), mappedStatement, boundSql, page, connection);
                    if (page.getTotal() <= 0L) {
                        return null;
                    }
                }

                DbType dbType = Optional.ofNullable(this.dbType).orElse(JdbcUtils.getDbType(connection.getMetaData().getURL()));
                IDialect dialect = Optional.ofNullable(this.dialect).orElse(DialectFactory.getDialect(dbType));
                String buildSql = concatOrderBy(originalSql, page);
                DialectModel model = dialect.buildPaginationSql(buildSql, page.offset(), page.getSize());
                Configuration configuration = mappedStatement.getConfiguration();
                List<ParameterMapping> mappings = new ArrayList(boundSql.getParameterMappings());
                Map<String, Object> additionalParameters = (Map) metaObject.getValue("delegate.boundSql.additionalParameters");
                model.consumers(mappings, configuration, additionalParameters);
                metaObject.setValue("delegate.boundSql.sql", model.getDialectSql());
                metaObject.setValue("delegate.boundSql.parameterMappings", mappings);
                return invocation.proceed();
            } else {
                return invocation.proceed();
            }
        } else {
            return invocation.proceed();
        }
    }

    /**
     * 处理分页数量
     *
     * @param page 分页参数
     */
    protected void handlerLimit(IPage<?> page) {
        page.setSize(this.limit);
    }

    /**
     * 查询总数量
     *
     * @param sql               sql语句
     * @param mappedStatement   映射语句包装类
     * @param boundSql          sql包装类
     * @param page              分页参数
     * @param connection        JDBC连接包装类
     */
    protected void queryTotal(String sql, MappedStatement mappedStatement, BoundSql boundSql, IPage<?> page, Connection connection) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            Throwable var7 = null;

            try {
                DefaultParameterHandler parameterHandler = new MybatisDefaultParameterHandler(mappedStatement, boundSql.getParameterObject(), boundSql);
                parameterHandler.setParameters(statement);
                long total = 0L;
                ResultSet resultSet = statement.executeQuery();
                Throwable var12 = null;

                try {
                    if (resultSet.next()) {
                        total = resultSet.getLong(1);
                    }
                } catch (Throwable var37) {
                    var12 = var37;
                    throw var37;
                } finally {
                    if (resultSet != null) {
                        if (var12 != null) {
                            try {
                                resultSet.close();
                            } catch (Throwable var36) {
                                var12.addSuppressed(var36);
                            }
                        } else {
                            resultSet.close();
                        }
                    }

                }

                page.setTotal(total);
                if (this.overflow && page.getCurrent() > page.getPages()) {
                    this.handlerOverflow(page);
                }
            } catch (Throwable var39) {
                var7 = var39;
                throw var39;
            } finally {
                if (statement != null) {
                    if (var7 != null) {
                        try {
                            statement.close();
                        } catch (Throwable var35) {
                            var7.addSuppressed(var35);
                        }
                    } else {
                        statement.close();
                    }
                }

            }

        } catch (Exception var41) {
            throw ExceptionUtils.mpe("Error: Method queryTotal execution error of sql : \n %s \n", var41, new Object[]{sql});
        }
    }

    /**
     * 设置默认当前页
     *
     * @param page 分页参数
     */
    protected void handlerOverflow(IPage<?> page) {
        page.setCurrent(1L);
    }

    /**
     * MybatisPlus拦截器实现自定义插件
     *
     * @param target 拦截目标对象
     * @return
     */
    @Override
    public Object plugin(Object target) {
        return target instanceof StatementHandler ? Plugin.wrap(target, this) : target;
    }

    /**
     * MybatisPlus拦截器实现自定义属性设置
     *
     * @param prop 属性参数
     */
    @Override
    public void setProperties(Properties prop) {
        String dialectType = prop.getProperty("dialectType");
        String dialectClazz = prop.getProperty("dialectClazz");
        if (StringUtils.isNotBlank(dialectType)) {
            this.setDialectType(dialectType);
        }

        if (StringUtils.isNotBlank(dialectClazz)) {
            this.setDialectClazz(dialectClazz);
        }

    }

    /**
     * 设置数据源类型
     *
     * @param dialectType 数据源类型
     */
    @Deprecated
    public void setDialectType(String dialectType) {
        this.setDbType(DbType.getDbType(dialectType));
    }


    /**
     * 设置方言实现类配置
     *
     * @param dialectClazz 方言实现类
     */
    @Deprecated
    public void setDialectClazz(String dialectClazz) {
        this.setDialect(DialectFactory.getDialect(dialectClazz));
    }

    /**
     * 设置获取总数的sql解析器
     *
     * @param countSqlParser 总数的sql解析器
     * @return 自定义MybatisPlus拦截器
     */
    public PaginationInterceptor setCountSqlParser(final ISqlParser countSqlParser) {
        this.countSqlParser = countSqlParser;
        return this;
    }

    /**
     * 溢出总页数，设置第一页
     *
     * @param overflow 溢出总页数
     * @return 自定义MybatisPlus拦截器
     */
    public PaginationInterceptor setOverflow(final boolean overflow) {
        this.overflow = overflow;
        return this;
    }

    /**
     * 设置分页规则
     *
     * @param limit 分页数量
     * @return 自定义MybatisPlus拦截器
     */
    public PaginationInterceptor setLimit(final long limit) {
        this.limit = limit;
        return this;
    }

    /**
     * 设置数据类型
     *
     * @param dbType 数据类型
     * @return 自定义MybatisPlus拦截器
     */
    public PaginationInterceptor setDbType(final DbType dbType) {
        this.dbType = dbType;
        return this;
    }

    /**
     * 设置方言
     *
     * @param dialect 方言
     * @return 自定义MybatisPlus拦截器
     */
    public PaginationInterceptor setDialect(final IDialect dialect) {
        this.dialect = dialect;
        return this;
    }


}

