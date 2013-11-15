StackSync Android client
========================


**Table of Contents**

- [Introduction](#introduction)
- [Architecture](#architecture)
- [Android client](#android-client)
- [Requirements](#requirements)
- [Setup](#setup)
- [Issue Tracking](#issue-tracking)
- [Licensing](#licensing)
- [Contact](#contact)


# Introduction

StackSync (<http://stacksync.com>) is a scalable open source Personal Cloud
that implements the basic components to create a synchronization tool.


# Architecture

In general terms, StackSync can be divided into three main blocks: clients
(desktop and mobile), synchronization service (SyncService) and storage
service (Swift, Amazon S3, FTP...). An overview of the architecture
with the main components and their interaction is shown in the following image.

<p align="center">
  <img width="500" src="https://raw.github.com/stacksync/desktop/master/res/stacksync-architecture.png">
</p>

The StackSync client and the SyncService interact through the communication
middleware called ObjectMQ. The sync service interacts with the metadata
database. The StackSync client directly interacts with the storage back-end
to upload and download files.

As storage back-end we are using OpenStack Swift, an open source cloud storage
software where you can store and retrieve lots of data in virtual containers.
It's based on the Cloud Files offering from Rackspace. But it is also possible
to use other storage back-ends, such as a FTP server or S3.


# Android client

Unlike the desktop client, the mobile app will not synchronize a local folder into a remote
repository. Synchronization would require the application to keep a local copy of the
repository in the local file system, which is not feasible due to the storage limitations present on
mobile devices.

Therefore, this client communicates with StackSync through an API. (We will upload the API soon!)



# Requirements

* Java 1.6 or newer
* ActionBarSherlock 

# Setup

The project is being developed using the Eclipse IDE. 

1. Import the ActionBarSherlock project.
2. Import the Android client project.
3. Go to propterties/android and add the ActionBarSherlock project as a library.
4. In the same window select the build target to Android 4.2 (API level 17) or newer. (Don't worry, it will work on Android 2.2 anyway).
5. Clean all projects
6. Now you are good to go!


To generate the APK installer file you can just export the project from Eclipse with the option "Export Android Application".


# Issue Tracking
We use the GitHub issue tracking.

# Licensing
StackSync is licensed under the GPLv3. Check [LICENSE](LICENSE) for the latest
licensing information.

# Contact
Visit www.stacksync.com for contact information.
