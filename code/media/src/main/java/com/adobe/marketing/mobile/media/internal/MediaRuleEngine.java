/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.media.internal;

import com.adobe.marketing.mobile.services.Log;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// MediaRuleEngine
class MediaRuleEngine {
    private static final String LOG_TAG = "MediaRuleEngine";
    private static final String RULE_NOT_FOUND = "Matching rule not found";
    private final Map<Integer, MediaRule> rulesMap;
    private IMediaRuleCallback enterFunction, exitFunction;

    public MediaRuleEngine() {
        rulesMap = new HashMap<>();
    }

    public boolean addRule(final MediaRule rule) {
        if (rulesMap.containsKey(rule.getName())) {
            return false;
        }

        rulesMap.put(rule.getName(), rule);
        return true;
    }

    public void onEnterRule(final IMediaRuleCallback enterFunction) {
        this.enterFunction = enterFunction;
    }

    public void onExitRule(final IMediaRuleCallback exitFunction) {
        this.exitFunction = exitFunction;
    }

    public MediaRuleResponse processRule(final int ruleName) {
        Map<String, Object> context = new HashMap<>();
        return processRule(ruleName, context);
    }

    public MediaRuleResponse processRule(final int ruleName, final Map<String, Object> context) {

        if (!rulesMap.containsKey(ruleName)) {
            return new MediaRuleResponse(false, RULE_NOT_FOUND);
        }

        MediaRule rule = rulesMap.get(ruleName);
        MediaRuleResponse response = rule.runPredicates(context);

        do {
            if (!response.isValid) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "processRule - Predicates failed for MediaRule " + rule.getDescription());
                break;
            }

            if (enterFunction != null && !enterFunction.call(rule, context)) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "processRule - Enter actions prevents further processing for MediaRule "
                                + rule.getDescription());
                break;
            }

            if (!rule.runActions(context)) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "processRule - MediaRule action prevents further processing for MediaRule "
                                + rule.getDescription());
                break;
            }

            if (exitFunction != null) {
                exitFunction.call(rule, context);
            }

        } while (false);

        return response;
    }
}

// MediaRule
class MediaRule {

    private final int name;
    private final String description;
    private final List<MediaPredicate> predicateList;
    private final List<IMediaRuleCallback> actionList;

    MediaRule(final int name, final String description) {

        this.name = name;
        this.description = description;
        this.actionList = new LinkedList<>();
        this.predicateList = new LinkedList<>();
    }

    int getName() {
        return this.name;
    }

    String getDescription() {
        return this.description;
    }

    MediaRule addPredicate(
            final IMediaRuleCallback predicateFn,
            final boolean expectedVal,
            final String errorString) {
        MediaPredicate predicate = new MediaPredicate(predicateFn, expectedVal, errorString);
        predicateList.add(predicate);
        return this;
    }

    MediaRule addAction(final IMediaRuleCallback actionFn) {
        actionList.add(actionFn);
        return this;
    }

    MediaRuleResponse runPredicates(final Map<String, Object> context) {
        for (MediaPredicate predicate : predicateList) {
            IMediaRuleCallback predicateFn = predicate.fn;
            boolean expectedVal = predicate.expectedValue;

            if (predicateFn.call(null, context) != expectedVal) {
                return new MediaRuleResponse(false, predicate.msg);
            }
        }

        return new MediaRuleResponse(true, "");
    }

    boolean runActions(final Map<String, Object> context) {
        for (IMediaRuleCallback action : actionList) {
            boolean retVal = action.call(null, context);

            if (!retVal) {
                return false;
            }
        }

        return true;
    }
}

// IMediaRuleCallback
interface IMediaRuleCallback {
    boolean call(final MediaRule rule, final Map<String, Object> context);
}

// MediaRuleResponse
class MediaRuleResponse {
    final boolean isValid;
    final String message;

    MediaRuleResponse(final boolean isValid, final String message) {
        this.isValid = isValid;
        this.message = message;
    }
}

// Predicate
class MediaPredicate {

    final IMediaRuleCallback fn;
    final boolean expectedValue;
    final String msg;

    MediaPredicate(final IMediaRuleCallback fn, final boolean expectedValue, final String msg) {
        this.fn = fn;
        this.expectedValue = expectedValue;
        this.msg = msg;
    }
}
