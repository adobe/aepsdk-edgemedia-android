/*************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2021 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 **************************************************************************/

package com.adobe.mediaanalyticstestapp;

import android.app.Application;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Analytics;
import com.adobe.marketing.mobile.Assurance;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.Media;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.VisitorID;

import java.util.Arrays;
import java.util.HashMap;

public class MediaAnalyticsTestApp extends Application {
	//Insert Launch App id
	private static final String LAUNCH_ENVIRONMENT_FILE_ID = "";

	@Override
	public void onCreate() {
		super.onCreate();
		MobileCore.setApplication(this);
		MobileCore.setLogLevel(LoggingMode.VERBOSE);

		MobileCore.registerExtensions(
				Arrays.asList(Assurance.EXTENSION,
						Identity.EXTENSION,
						Analytics.EXTENSION,
						Media.EXTENSION),
				(AdobeCallback) o -> MobileCore.configureWithAppID(LAUNCH_ENVIRONMENT_FILE_ID));
	}
}