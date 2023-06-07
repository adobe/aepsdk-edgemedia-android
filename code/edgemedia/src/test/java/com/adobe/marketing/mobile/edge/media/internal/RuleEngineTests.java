/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.edge.media.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RuleEngineTests {

    @Mock IMediaRuleCallback mockActionCallback, mockActionCallback2;

    @Before
    public void testSetup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_getRuleNameAndDescription() {
        int ruleName = 1;
        String ruleDescription = "Rule 1";
        MediaRule rule = new MediaRule(ruleName, ruleDescription);

        assertEquals("Rule 1", rule.getDescription());
        assertEquals(ruleName, rule.getName());
    }

    @Test
    public void test_addRule_success() {
        MediaRuleEngine ruleEngine = new MediaRuleEngine();
        MediaRule rule = new MediaRule(1, "Rule 1");

        assertTrue(ruleEngine.addRule(rule));
    }

    @Test
    public void test_addRule_duplicate_fail() {
        MediaRuleEngine ruleEngine = new MediaRuleEngine();
        MediaRule rule1 = new MediaRule(1, "Rule 1");
        ruleEngine.addRule(rule1);

        MediaRule rule2 = new MediaRule(1, "Rule 1");
        assertFalse(ruleEngine.addRule(rule2));
    }

    @Test
    public void test_processRule_noMatchingRule_fail() {
        MediaRuleEngine ruleEngine = new MediaRuleEngine();
        MediaRuleResponse res = ruleEngine.processRule(1);
        assertFalse(res.isValid);
        assertEquals("Matching rule not found", res.message);
    }

    @Test
    public void test_processRule_noPredicate_success() {
        MediaRuleEngine ruleEngine = new MediaRuleEngine();

        MediaRule rule = new MediaRule(1, "Rule 1");
        rule.addAction(mockActionCallback);

        ruleEngine.addRule(rule);

        MediaRuleResponse res = ruleEngine.processRule(1);

        verify(mockActionCallback, times(1)).call(eq(null), ArgumentMatchers.anyMap());
        assertTrue(res.isValid);
    }

    @Test
    public void test_processRule_noAction_success() {
        MediaRuleEngine ruleEngine = new MediaRuleEngine();

        MediaRule rule = new MediaRule(1, "Rule 1");
        rule.addPredicate((rule1, context) -> true, true, "Error1");

        ruleEngine.addRule(rule);

        MediaRuleResponse res = ruleEngine.processRule(1);
        assertTrue(res.isValid);
    }

    @Test
    public void test_processRule_failedPredicates_fail() {
        MediaRuleEngine ruleEngine = new MediaRuleEngine();
        String ruleError1 = "error message 1";
        String ruleError2 = "error message 2";

        MediaRule rule = new MediaRule(1, "Rule 1");

        rule.addPredicate((rule1, context) -> true, true, ruleError1);

        rule.addPredicate((rule12, context) -> true, false, ruleError2);

        ruleEngine.addRule(rule);

        MediaRuleResponse res = ruleEngine.processRule(1);
        assertFalse(res.isValid);
        assertEquals(ruleError2, res.message);
    }

    @Test
    public void test_processRule_executeAllActions_success() {
        MediaRuleEngine ruleEngine = new MediaRuleEngine();

        MediaRule rule = new MediaRule(1, "Rule 1");

        rule.addPredicate((rule1, context) -> true, true, "");

        rule.addAction(
                (rule13, context) -> {
                    mockActionCallback.call(null, context);
                    return true;
                });

        rule.addAction(
                (rule12, context) -> {
                    mockActionCallback.call(null, context);
                    return true;
                });

        ruleEngine.addRule(rule);

        MediaRuleResponse res = ruleEngine.processRule(1);
        verify(mockActionCallback, times(2)).call(eq(null), ArgumentMatchers.anyMap());
        assertTrue(res.isValid);
    }

    @Test
    public void test_processRule_executeActions_stopAfterFailedAction() {
        MediaRuleEngine ruleEngine = new MediaRuleEngine();
        MediaRule rule = new MediaRule(1, "Rule 1");

        rule.addPredicate((rule1, context) -> true, true, "");

        rule.addAction(
                (rule13, context) -> {
                    mockActionCallback.call(null, context);
                    return false;
                });

        rule.addAction(
                (rule12, context) -> {
                    mockActionCallback2.call(null, context);
                    return true;
                });

        ruleEngine.addRule(rule);

        MediaRuleResponse res = ruleEngine.processRule(1);
        verify(mockActionCallback, times(1)).call(eq(null), ArgumentMatchers.anyMap());
        verify(mockActionCallback2, times(0)).call(eq(null), ArgumentMatchers.anyMap());
        assertTrue(res.isValid);
    }

    @Test
    public void test_processRule_enterExitFunction() {
        MediaRuleEngine ruleEngine = new MediaRuleEngine();
        MediaRule rule = new MediaRule(1, "Rule 1");

        rule.addPredicate((rule1, context) -> true, true, "");

        rule.addAction((rule12, context) -> true);

        ruleEngine.onEnterRule(
                (rule13, context) -> {
                    mockActionCallback.call(rule13, context);
                    return true;
                });

        ruleEngine.onExitRule(
                (rule14, context) -> {
                    mockActionCallback2.call(rule14, context);
                    return true;
                });

        ruleEngine.addRule(rule);

        MediaRuleResponse res = ruleEngine.processRule(1);
        verify(mockActionCallback, times(1)).call(any(MediaRule.class), ArgumentMatchers.anyMap());
        verify(mockActionCallback2, times(1)).call(any(MediaRule.class), ArgumentMatchers.anyMap());
        assertTrue(res.isValid);
    }

    @Test
    public void test_processRule_stopAfterFailedEnterAction() {
        MediaRuleEngine ruleEngine = new MediaRuleEngine();
        MediaRule rule = new MediaRule(1, "Rule 1");

        rule.addPredicate((rule1, context) -> true, true, "");

        rule.addAction((rule12, context) -> true);

        ruleEngine.onEnterRule(
                (rule14, context) -> {
                    mockActionCallback.call(rule14, context);
                    return false;
                });

        ruleEngine.onExitRule(
                (rule13, context) -> {
                    mockActionCallback2.call(rule13, context);
                    return true;
                });

        ruleEngine.addRule(rule);

        MediaRuleResponse res = ruleEngine.processRule(1);
        verify(mockActionCallback, times(1)).call(any(MediaRule.class), ArgumentMatchers.anyMap());
        verify(mockActionCallback2, times(0)).call(any(MediaRule.class), ArgumentMatchers.anyMap());
        assertTrue(res.isValid);
    }

    @Test
    public void test_processRule_passContextData() {
        MediaRuleEngine ruleEngine = new MediaRuleEngine();
        MediaRule rule = new MediaRule(1, "Rule 1");

        rule.addPredicate(
                (rule1, context) -> {
                    assertTrue(context.containsKey("k1"));

                    if (context.containsKey("k1")) {
                        assertEquals("v1", context.get("k1"));
                    }

                    context.put("k1", "v2");
                    return true;
                },
                true,
                "");

        rule.addAction(
                (rule12, context) -> {
                    assertTrue(context.containsKey("k1"));

                    if (context.containsKey("k1")) {
                        assertEquals("v2", context.get("k1"));
                    }

                    return true;
                });

        ruleEngine.addRule(rule);

        Map<String, Object> context = new HashMap<>();
        context.put("k1", "v1");

        MediaRuleResponse res = ruleEngine.processRule(1, context);
        assertTrue(res.isValid);
    }
}
