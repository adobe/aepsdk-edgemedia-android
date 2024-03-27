EXTENSION-LIBRARY-FOLDER-NAME = edgemedia
TEST-APP-FOLDER-NAME = testapp
KOTLIN-TEST-APP-FOLDER-NAME = testapp-kotlin

init:
	git config core.hooksPath .githooks

clean:
	(./code/gradlew -p code clean)

checkstyle: 
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) checkstyle)

checkformat:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) spotlessCheck)
	(./code/gradlew -p code/$(TEST-APP-FOLDER-NAME) spotlessCheck)
	(./code/gradlew -p code/$(KOTLIN-TEST-APP-FOLDER-NAME) spotlessCheck)

format:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) spotlessApply)
	(./code/gradlew -p code/$(TEST-APP-FOLDER-NAME) spotlessApply)
	(./code/gradlew -p code/$(KOTLIN-TEST-APP-FOLDER-NAME) spotlessApply)

format-license:
	(./code/gradlew -p code licenseFormat)

unit-test:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) testPhoneDebugUnitTest)

unit-test-coverage:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) createPhoneDebugUnitTestCoverageReport)

functional-test:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) uninstallPhoneDebugAndroidTest)
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) connectedPhoneDebugAndroidTest)		

functional-test-coverage:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) uninstallPhoneDebugAndroidTest)
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) createPhoneDebugAndroidTestCoverageReport)

javadoc:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) javadocJar)

assemble-phone:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) assemblePhone)

assemble-phone-debug:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME)  assemblePhoneDebug)

assemble-phone-release:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME)  assemblePhoneRelease)

assemble-app:
	(./code/gradlew -p code/$(TEST-APP-FOLDER-NAME) assemble)
	(./code/gradlew -p code/$(KOTLIN-TEST-APP-FOLDER-NAME) assemble)

ci-publish-maven-local-jitpack:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) publishReleasePublicationToMavenLocal -Pjitpack  -x signReleasePublication)

ci-publish-staging:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) publishReleasePublicationToSonatypeRepository)

ci-publish:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) publishReleasePublicationToSonatypeRepository -Prelease)