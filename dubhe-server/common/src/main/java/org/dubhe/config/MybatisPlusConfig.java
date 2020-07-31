/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.config;

import com.baomidou.mybatisplus.core.override.MybatisMapperProxy;
import com.baomidou.mybatisplus.core.parser.ISqlParser;
import com.baomidou.mybatisplus.core.parser.SqlParserHelper;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantHandler;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantSqlParser;
import com.google.common.collect.Sets;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.dubhe.annotation.DataPermission;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.constant.PermissionConstant;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.entity.Role;
import org.dubhe.enums.LogEnum;
import org.dubhe.utils.JwtUtils;
import org.dubhe.utils.LogUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description MybatisPlus配置类
 * @date 2020-06-24
 */
@EnableTransactionManagement
@Configuration
public class MybatisPlusConfig implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

    /**
     * 以此字段作为租户实现数据隔离
     */
    private static final String TENANT_ID_COLUMN = "create_user_id";
    /**
     * 以0作为公共数据的标识
     */
    private static final long PUBLIC_TENANT_ID = MagicNumConstant.ZERO;
    private static final Set<Long> PUBLIC_TENANT_ID_SET = new HashSet<Long>() {{
        add(PUBLIC_TENANT_ID);
    }};
    private static final String PACKAGE_SEPARATOR = ".";
    private static final Set<String> SELECT_PERMISSION = new HashSet<String>() {{
        add(PermissionConstant.SELECT);
    }};
    private static final Set<String> UPDATE_DELETE_PERMISSION = new HashSet<String>() {{
        add(PermissionConstant.UPDATE);
        add(PermissionConstant.DELETE);
    }};

    private static final String SELECT_STR = "select";
    /**
     * 优先级高于dataFilters，如果ignore，则不进行sql注入
     */
    private Map<String, Set<String>> dataFilters = new HashMap<>();

    private ApplicationContext applicationContext;
    public Set<Long> tenantId;

    /**
     * mybatis plus 分页插件
     * 其中增加了通过多租户实现了数据权限功能
     *
     * @return
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        List<ISqlParser> sqlParserList = new ArrayList<>();
        TenantSqlParser tenantSqlParser = new TenantSqlParser();
        tenantSqlParser.setTenantHandler(new TenantHandler() {
            @Override
            public Expression getTenantId(boolean where) {
                Set<Long> tenants = tenantId;

                final boolean multipleTenantIds = tenants.size() > MagicNumConstant.ONE;
                if (multipleTenantIds) {
                    return multipleTenantIdCondition(tenants);
                } else {
                    return singleTenantIdCondition(tenants);
                }
            }

            private Expression singleTenantIdCondition(Set<Long> tenants) {
                return new LongValue((Long) tenants.toArray()[0]);
            }

            private Expression multipleTenantIdCondition(Set<Long> tenants) {
                final InExpression inExpression = new InExpression();
                inExpression.setLeftExpression(new Column(getTenantIdColumn()));
                final ExpressionList itemsList = new ExpressionList();
                final List<Expression> inValues = new ArrayList<>(tenants.size());
                tenants.forEach(i ->
                        inValues.add(new LongValue(i))
                );
                itemsList.setExpressions(inValues);
                inExpression.setRightItemsList(itemsList);
                return inExpression;
            }

            @Override
            public String getTenantIdColumn() {
                return TENANT_ID_COLUMN;
            }

            @Override
            public boolean doTableFilter(String tableName) {
                return false;
            }
        });
        sqlParserList.add(tenantSqlParser);
        paginationInterceptor.setSqlParserList(sqlParserList);
        paginationInterceptor.setSqlParserFilter(metaObject -> {
            MappedStatement ms = SqlParserHelper.getMappedStatement(metaObject);
            String method = ms.getId();
            if (!dataFilters.containsKey(method) || isAdmin()) {
                return true;
            }
            Set<String> permission = dataFilters.get(method);
            tenantId = getTenantId(permission);
            return false;
        });
        return paginationInterceptor;
    }

    /**
     * 判断用户是否是管理员
     * 如果未登录，无法请求任何接口，所以不会到该层，因此匿名认为是定时任务，给予admin权限。
     *
     * @return 判断用户是否是管理员
     */
    private boolean isAdmin() {
        UserDTO user;
        try {
            user = JwtUtils.getCurrentUserDto();
        } catch (UnavailableSecurityManagerException e) {
            return true;
        }
        if (Objects.isNull(user)) {
            return true;
        }
        List<Role> roles;
        if ((roles = user.getRoles()) == null) {
            return false;
        }
        Set<String> permissions = roles.stream().map(Role::getPermission).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(permissions)) {
            return false;
        }
        return user.getId() == PermissionConstant.ANONYMOUS_USER || user.getId() == PermissionConstant.ADMIN_USER_ID;
    }

    /**
     * 如果是管理员，在前一步isAdmin已过滤；
     * 如果是匿名用户，在shiro层被过滤；
     * 因此只会是无角色、权限用户或普通用户
     *
     * @return Set<Long> 租户ID集合
     */
    private Set<Long> getTenantId(Set<String> permission) {
        UserDTO user = JwtUtils.getCurrentUserDto();
        List<Role> roles;
        if (Objects.isNull(user) || (roles = user.getRoles()) == null) {
            if (permission.contains(PermissionConstant.SELECT)) {
                return PUBLIC_TENANT_ID_SET;
            }
            return Collections.EMPTY_SET;
        }
        Set<String> permissions = roles.stream().map(Role::getPermission).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(permissions)) {
            if (permission.contains(PermissionConstant.SELECT)) {
                return PUBLIC_TENANT_ID_SET;
            }
            return Collections.EMPTY_SET;
        }
        if (permission.contains(PermissionConstant.SELECT)) {
            return new HashSet<Long>() {{
                add(PUBLIC_TENANT_ID);
                add(user.getId());
            }};
        }
        return new HashSet<Long>() {{
            add(user.getId());
        }};
    }

    /**
     * 设置上下文
     * #需要通过上下文 获取SpringBean
     *
     * @param applicationContext spring上下文
     * @throws BeansException 找不到bean异常
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        Class<? extends Annotation> annotationClass = DataPermission.class;
        Map<String, Object> beanWithAnnotation = applicationContext.getBeansWithAnnotation(annotationClass);
        Set<Map.Entry<String, Object>> entitySet = beanWithAnnotation.entrySet();
        for (Map.Entry<String, Object> entry : entitySet) {
            Proxy proxy = (Proxy) entry.getValue();
            Class clazz = getMapperClass(proxy);
            populateDataFilters(clazz);
        }
    }

    /**
     * 根据mapper对应代理对象获取Class
     *
     * @param proxy mapper对应代理对象
     * @return
     */
    private Class getMapperClass(Proxy proxy) {
        try {
            Field field = proxy.getClass().getSuperclass().getDeclaredField("h");
            field.setAccessible(true);
            MybatisMapperProxy mapperProxy = (MybatisMapperProxy) field.get(proxy);
            field = mapperProxy.getClass().getDeclaredField("mapperInterface");
            field.setAccessible(true);
            return (Class) field.get(mapperProxy);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "reflect error", e);
        }
        return null;
    }

    /**
     * 填充数据权限过滤，处理那些需要排除的方法
     *
     * @param clazz 需要处理的类(mapper)
     */
    private void populateDataFilters(Class clazz) {
        if (clazz == null) {
            return;
        }
        Method[] methods = clazz.getMethods();
        DataPermission dataPermission = AnnotationUtils.findAnnotation((Class<?>) clazz, DataPermission.class);
        Set<String> ignores = Sets.newHashSet(dataPermission.ignores());
        for (Method method : methods) {
            if (ignores.contains(method.getName())) {
                continue;
            }
            Set<String> permission = getDataPermission(method);
            dataFilters.put(clazz.getName() + PACKAGE_SEPARATOR + method.getName(), permission);
        }
    }

    /**
     * 获取方法上权限注解
     * 权限注解包含
     * 1.用户拥有指定权限才可以执行该方法：比如 PermissionConstant.SELECT 表示用户必须拥有select权限，才可以使用该方法
     * 2.方法权限校验排除：比如 ignores = {"insert"} 表示insert方法不做权限处理
     *
     * @param method 方法对象
     * @return
     */
    private Set<String> getDataPermission(Method method) {
        DataPermission dataPermission = AnnotationUtils.findAnnotation(method, DataPermission.class);
        // 无注解时以方法名判断
        if (dataPermission == null) {
            if (method.getName().contains(SELECT_STR)) {
                return SELECT_PERMISSION;
            }
            return UPDATE_DELETE_PERMISSION;
        }
        return Sets.newHashSet(dataPermission.permission());
    }

}
