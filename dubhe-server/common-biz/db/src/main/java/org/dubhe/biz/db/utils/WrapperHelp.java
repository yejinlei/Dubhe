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

package org.dubhe.biz.db.utils;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.dubhe.biz.db.annotation.Query;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @description  构建Wrapper
 * @date 2020-03-15
 */
@Slf4j
public class WrapperHelp {

    public static <T> QueryWrapper getWrapper(T query) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        if (query == null) {
            return queryWrapper;
        }
        try {
            List<Field> fields = getAllFields(query.getClass(), new ArrayList<>());
            for (Field field : fields) {
                field.setAccessible(true);
                Query q = field.getAnnotation(Query.class);
                if (q != null) {
                    String propName = q.propName();
                    String attributeName = isBlank(propName) ? field.getName() : propName;
                    Object val = field.get(query);
                    if (val == null) {
                        continue;
                    }
                    // 模糊多字段
                    String blurry = q.blurry();
                    if (ObjectUtil.isNotEmpty(blurry)) {
                        String[] blurrys = blurry.split(",");
                        queryWrapper.and(qw -> {
                            for (int i = 0; i < blurrys.length; i++) {
                                if (i == 0) {
                                    qw.like(blurrys[i], val);
                                } else {
                                    qw.or().like(blurrys[i], val);
                                }
                            }
                        });
                        continue;
                    }
                    switch (q.type()) {
                        case EQ:
                            queryWrapper = queryWrapper.eq(attributeName, val);
                            break;
                        case NE:
                            queryWrapper = queryWrapper.ne(attributeName, val);
                            break;
                        case GE:
                            queryWrapper = queryWrapper.ge(attributeName, val);
                            break;
                        case GT:
                            queryWrapper = queryWrapper.gt(attributeName, val);
                            break;
                        case LT:
                            queryWrapper = queryWrapper.lt(attributeName, val);
                            break;
                        case LE:
                            queryWrapper = queryWrapper.le(attributeName, val);
                            break;
                        case BETWEEN:
                            List<Object> between = new ArrayList<>((List<Object>) val);
                            queryWrapper = queryWrapper.between(attributeName, between.get(0), between.get(1));
                            break;
                        case NOT_BETWEEN:
                            List<Object> notBetween = new ArrayList<>((List<Object>) val);
                            queryWrapper = queryWrapper.notBetween(attributeName, notBetween.get(0), notBetween.get(1));
                            break;
                        case LIKE:
                            queryWrapper = queryWrapper.like(attributeName, val);
                            break;
                        case NOT_LIKE:
                            queryWrapper = queryWrapper.notLike(attributeName, val);
                            break;
                        case LIkE_LEFT:
                            queryWrapper = queryWrapper.likeLeft(attributeName, val);
                            break;
                        case LIKE_RIGHT:
                            queryWrapper = queryWrapper.likeRight(attributeName, val);
                            break;
                        case IS_NULL:
                            queryWrapper = queryWrapper.isNull(attributeName);
                            break;
                        case IS_NOT_NULL:
                            queryWrapper = queryWrapper.isNotNull(attributeName);
                            break;
                        case IN:
                            queryWrapper = queryWrapper.in(attributeName, (Collection) val);
                            break;
                        case NOT_IN:
                            queryWrapper = queryWrapper.notIn(attributeName, (Collection) val);
                            break;
                        case INSQL:
                            queryWrapper = queryWrapper.inSql(attributeName, String.valueOf(val));
                            break;
                        case NOT_INSQL:
                            queryWrapper = queryWrapper.notInSql(attributeName, String.valueOf(val));
                            break;
                        case ORDER_BY:
                            queryWrapper = queryWrapper.last(" ORDER BY " + val);
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return queryWrapper;
    }

    private static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static List<Field> getAllFields(Class clazz, List<Field> fields) {
        if (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            getAllFields(clazz.getSuperclass(), fields);
        }
        return fields;
    }

}
