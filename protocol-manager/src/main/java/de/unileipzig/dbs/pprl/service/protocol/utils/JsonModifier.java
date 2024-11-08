/*
 * Copyright © 2018 - 2020 Leipzig University (Database Research Group)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.unileipzig.dbs.pprl.service.protocol.utils;

import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.internal.JsonFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonModifier {

    public static String fullClassSelector(String parent, String fullClassName) {
        return parent + "[?(@.@class=='" + fullClassName + "')]";
    }

    public static String classSelector(String parent, String className) {
        return fullClassSelector(parent, "." + className);
    }

    public static Filter classFilter(String className) {
        return Filter.filter(
                Criteria.where("@.@class").eq(className)
        );
    }

    public static String set(String jsonString, String jsonpath, Object value) {
        return set(jsonString, JsonPath.compile(jsonpath), value);
    }

    public static String set(String jsonString, JsonPath jsonpath, Object value) {
        DocumentContext context = JsonPath.parse(jsonString);
        context.set(jsonpath, value);
        return context.jsonString();
    }

    public static String put(String jsonString, String jsonpath, String key, Object value) {
        return put(jsonString, JsonPath.compile(jsonpath), key, value);
    }

    public static String put(String jsonString, JsonPath jsonpath, String key, Object value) {
        DocumentContext context = JsonPath.parse(jsonString);
        context.put(jsonpath, key, value);
        return context.jsonString();
    }

    public static boolean test(String jsonString, String jsonPath) {
        return test(jsonString, JsonPath.compile(jsonPath));
    }

    public static boolean test(String jsonString, JsonPath jsonPath) {
        try {
            read(jsonString, jsonPath);
            return true;
        } catch (PathNotFoundException e) {
            return false;
        }
    }

    public static Object read(String jsonString, String jsonPath) {
        return read(jsonString, JsonPath.compile(jsonPath));
    }

    public static Object read(String jsonString, JsonPath jsonPath) throws PathNotFoundException {
        DocumentContext context = JsonPath.parse(jsonString);
        return context.read(jsonPath);
    }

    public static String append(String jsonString, String path, Filter filter, String attribute, String postfix) {
        Object object = JsonModifier.read(jsonString, JsonPath.compile(path, filter));
        List<String> originalValues = new ArrayList<>();
        if (object instanceof String) {
            originalValues = Collections.singletonList((String)object);
        } else if (object instanceof List) {
            originalValues = (List<String>)object;
        }
        System.out.println(originalValues);

        String out = jsonString;
        for (String originalValue : originalValues) {
            Filter replaceSelect = filter.and(
                    Criteria.where(attribute).eq(originalValue)
            );
            JsonPath jsonPath = JsonPath.compile(path, replaceSelect);
            out = JsonModifier.set(out, jsonPath, originalValue + postfix);
        }
        return out;
    }

    public static String prettify(String jsonString) {
        return JsonFormatter.prettyPrint(jsonString);
    }
}
