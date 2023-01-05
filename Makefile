EXTENSION-LIBRARY-FOLDER-NAME = android-media-library
TEST-APP-FOLDER-NAME = testapp

BUILD-ASSEMBLE-LOCATION = ./ci/assemble
ROOT_DIR=$(shell git rev-parse --show-toplevel)

PROJECT_NAME = $(shell cat $(ROOT_DIR)/code/gradle.properties | grep "moduleProjectName" | cut -d'=' -f2)
AAR_NAME = $(shell cat $(ROOT_DIR)/code/gradle.properties | grep "moduleAARName" | cut -d'=' -f2)
MODULE_NAME = $(shell cat $(ROOT_DIR)/code/gradle.properties | grep "moduleName" | cut -d'=' -f2)
LIB_VERSION = $(shell cat $(ROOT_DIR)/code/gradle.properties | grep "moduleVersion" | cut -d'=' -f2)
SOURCE_FILE_DIR =  $(ROOT_DIR)/code/$(PROJECT_NAME)
AAR_FILE_DIR =  $(ROOT_DIR)/code/$(PROJECT_NAME)/build/outputs/aar

clean:
	(rm -rf $(AAR_FILE_DIR))
	(./code/gradlew -p code clean)

checkstyle: 
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) checkstyle)

checkformat:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) spotlessCheck)

format:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) spotlessApply)

unit-test:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) testPhoneDebugUnitTest)

unit-test-coverage:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) createPhoneDebugUnitTestCoverageReport)

functional-test:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) uninstallPhoneDebugAndroidTest)
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) connectedPhoneDebugAndroidTest)		

functional-test-coverage:		
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) createPhoneDebugAndroidTestCoverageReport)

javadoc:
	(./code/gradlew -p code/$(EXTENSION-LIBRARY-FOLDER-NAME) javadocPublish)

assemble-phone:
		(./code/gradlew -p code/${EXTENSION-LIBRARY-FOLDER-NAME}  assemblePhone)

assemble-phone-release:		
		(./code/gradlew -p code/${EXTENSION-LIBRARY-FOLDER-NAME}  assemblePhoneRelease)

build-release:
	(./code/gradlew -p code/${EXTENSION-LIBRARY-FOLDER-NAME} clean lint assemblePhoneRelease)

publish-maven-local:
	(./code/gradlew -p code/${EXTENSION-LIBRARY-FOLDER-NAME} assemblePhone)
	(./code/gradlew -p code/${EXTENSION-LIBRARY-FOLDER-NAME} publishReleasePublicationToMavenLocal -x signReleasePublication)

ci-publish-staging: clean build-release
	(./code/gradlew -p code/${EXTENSION-LIBRARY-FOLDER-NAME} publishReleasePublicationToSonatypeRepository --stacktrace)

ci-publish-main: clean build-release
	(./code/gradlew -p code/${EXTENSION-LIBRARY-FOLDER-NAME} publishReleasePublicationToSonatypeRepository -Prelease)

ci-publish-jitpack:
	(./code/gradlew -p code/${EXTENSION-LIBRARY-FOLDER-NAME} assemblePhone)
	(./code/gradlew -p code/${EXTENSION-LIBRARY-FOLDER-NAME} publishReleasePublicationToMavenLocal -Pjitpack -x signReleasePublication)
