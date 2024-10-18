!()[src/main/resources/assets/decoration/logo.png]
ResourceRush is a classic tower game. You purchase towers that generate resources. These resources are collected by 
carts as they travel along the path. Your goal is to optimize resource generation to achieve victory.

## Authors
- SENG201 Teaching team
- William Brown
- Corey Hines


## Dependencies
This application has the following dependencies:
- JDK >= 17 [click here to get the latest stable OpenJDK release (as of writing this README)](https://jdk.java.net/18/)
- JavaFX Controls 21.0.2 (https://mvnrepository.com/artifact/org.openjfx/javafx-controls)
- JavaFX FXML 21.0.2 (https://mvnrepository.com/artifact/org.openjfx/javafx-fxml)
- JavaFX Media 21.0.2 (https://mvnrepository.com/artifact/org.openjfx/javafx-media)
- JSON Simple 1.1.1 (https://mvnrepository.com/artifact/com.googlecode.json-simple/json-simple)
- JUnit Jupiter 5.10.2 (https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter)
- JUnit Jupiter API 5.10.2 (https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api)

The dependencies can be added to `build.gradle` as follows:
```gradle
dependencies {
    implementation "org.openjfx:javafx-controls:${javafxVersion}"
    implementation "org.openjfx:javafx-fxml:${javafxVersion}"
    implementation "org.openjfx:javafx-media:${javafxVersion}"
    implementation 'com.googlecode.json-simple:json-simple:1.1.1'
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.10.2'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
}
```


# Cloning
1. Open a terminal window
2. Use the `git clone` command to clone the repository
```
git clone https://eng-git.canterbury.ac.nz/seng201-2024/team-53.git
```
This will create a new directory named `team-53` containing the project files.


## Importing Project (Using IntelliJ)
IntelliJ has built-in support for Gradle. To import the repository:

- Launch IntelliJ and choose `Open` from the start-up window.
- Select the `team-53` directory and click open
- At this point in the bottom right notifications you may be prompted to 'load gradle scripts', If so, click load


## Run Project
1. Open a terminal inside the project directory.
2. Run the following gcommand
```
./gradlew run
```
This will build the project and launch the application in a new window.


## Build and Run Jar
1. Open a command line interface inside the project directory.
2. Run the following command
```
./gradlew jar
```
This will create a JAR file in the `build/libs/` directory named `seng201_team53-1.0-SNAPSHOT.jar`
3. Navigate to the `build/libs/` directory inside the project.
4. Run the following command to open the application.
```
java -jar seng201_team53-1.0-SNAPSHOT.jar
```
This will launch the application.


## Run Tests
1. Open a command line interface inside the project directory.
2. Run the following command
```
./gradlew test
```
This will execute all the unit tests for the project and display the results in the terminal.


## Credits 
The following free to use assets were used in this project:

craftpix.net (License: https://craftpix.net/file-licenses/)
- "Free Stone Tower Game Assets" - https://craftpix.net/freebies/free-stone-tower-game-assets/  
- "Free Battle Location Top Down Game Tileset Pack 1" - https://craftpix.net/freebies/free-battle-location-top-down-game-tileset-pack-1/
- "Free Top-Down Bushes Pixel Art" - https://craftpix.net/freebies/free-top-down-bushes-pixel-art/

freesound.org
- "piano groove 2.wav" by "timouse" (https://freesound.org/people/timouse/sounds/639565/) licensed under CCBY 4.0
- "projectile.wav" by "jeckkech" (https://freesound.org/people/jeckkech/sounds/391660/) licensed under CC0 1.0

gamedeveloperstudio.com (License: https://www.gamedeveloperstudio.com/license.php)
- "Windmill game asset" - https://www.gamedeveloperstudio.com/graphics/viewgraphic.php?item=1x481r9m1c7l9i8j84

kindpng.com
- "Dollar Pixel Art, HD Png Download" by "Clint Tucker" (https://www.kindpng.com/imgv/iwbTbxm_dollar-pixel-art-hd-png-download) licensed under Personal Use Only

flaticon.com (License: https://www.freepikcompany.com/legal/#nav-flaticon-agreement)
- "Fast Forward free icon" by "Pixel perfect" (https://www.flaticon.com/free-icon/fast-forward_776819)

itch.io
- "Spire - Tileset 1" by "Baldur", distributed by foozle.io (https://foozlecc.itch.io/spire-tileset-1) licensed under CC0 1.0
