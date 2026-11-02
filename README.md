# Dish It

Dish It is a JavaFX recipe app in `DishItFX`.

## Requirements

- JDK 24 or later
- Maven 3.9 or later
- VS Code with the Extension Pack for Java, or Eclipse IDE for Java Developers

JavaFX dependencies are managed by Maven; a separate JavaFX SDK installation is
not needed.

## Run in VS Code

Open this repository folder in VS Code, then run **Terminal: Run Task** and
select **Run Dish It**.

To run it from a terminal instead:

```text
mvn -f DishItFX/pom.xml javafx:run
```

To compile and package the app:

```text
mvn -f DishItFX/pom.xml clean package
```

## Run in Eclipse

1. Install Eclipse IDE for Java Developers and configure a JDK 24 or later.
2. Select **File > Import > Maven > Existing Maven Projects**.
3. Choose the `DishItFX` folder and finish the import.
4. Right-click the `DishItFX` project and select **Run As > Maven build...**.
5. Enter `javafx:run` as the goal and run the configuration.

Maven resolves JavaFX and its platform-specific libraries from `pom.xml`; you
do not need to add a JavaFX SDK to Eclipse's build path. To compile and package
from Eclipse, run the Maven goal `clean package` instead.
