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

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.dubhe.biz.base.annotation.DataPermission;
import org.dubhe.biz.base.context.DataContext;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.enums.OperationTypeEnum;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.permission.util.SqlUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;


/**
 * @description mybatis拦截器
 * @date 2020-11-25
 */
@Component
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class CustomerSqlInterceptor implements Interceptor {

    @Resource
    private UserContextService userContextService;
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
        /*
         * 先拦截到RoutingStatementHandler，里面有个StatementHandler类型的delegate变量，其实现类是BaseStatementHandler，
         * 然后就到BaseStatementHandler的成员变量mappedStatement
         */
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        //id为执行的mapper方法的全路径名，如com.uv.dao.UserDao.selectPageVo
        String id = mappedStatement.getId();
        //sql语句类型 select、delete、insert、update
        String sqlCommandType = mappedStatement.getSqlCommandType().toString();
        BoundSql boundSql = statementHandler.getBoundSql();

        //获取到原始sql语句
        String sql = boundSql.getSql();
        String mSql = sql;

        //注解逻辑判断  添加注解了才拦截
        Class<?> classType = Class.forName(mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf(".")));
        String mName = mappedStatement.getId().substring(mappedStatement.getId().lastIndexOf(".") + 1, mappedStatement.getId().length());

        //获取类注解 获取需要忽略拦截的方法名称
        DataPermission dataAnnotation = classType.getAnnotation(DataPermission.class);
        if (!Objects.isNull(dataAnnotation)) {
            UserContext curUser = userContextService.getCurUser();
            String[] ignores = dataAnnotation.ignoresMethod();
            //校验拦截忽略方法名 忽略新增方法 忽略回调/定时方法
            if ((!Objects.isNull(ignores) && Arrays.asList(ignores).contains(mName))
                    || OperationTypeEnum.INSERT.getType().equals(sqlCommandType.toLowerCase())
                    || Objects.isNull(curUser)
                    || (!Objects.isNull(DataContext.get()) && DataContext.get().getType())
            ) {
                return invocation.proceed();
            } else {
                //拦截所有sql操作类型
                mSql = SqlUtil.buildTargetSql(sql, SqlUtil.getResourceIds(curUser),curUser);
            }
        }

        //通过反射修改sql语句
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, mSql);
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }

}