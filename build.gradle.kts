buildscript {
  repositories {
    mavenCentral()
  }

  extra.apply {
    set("apacheCommonsLang3Version", "3.12.0")
    set("apacheCommonsTextVersion", "1.9")
    set("bootstrapVersion", "4.6.0-1")
    set("cacheApiVersion", "1.1.1")
    set("commonsCodecVersion", "1.15")
    set("datatablesVersion", "1.10.25")
    set("ehcacheVersion", "3.9.4")
    set("esapiVersion", "2.2.3.1")
    set("flywayVersion", "7.11.2")
    set("h2Version", "1.4.200")
    set("httpClientVersion", "4.5.13")
    set("mockitoVersion", "3.11.2")
    set("jacksonVersion", "2.12.4")
    set("jacocoVersion", "0.8.7")
    set("jaxbApiVersion", "2.3.1")
    set("jsonwebtokenVersion", "0.9.1")
    set("jsoupVersion", "1.14.1")
    set("junitVersion", "5.7.2")
    set("lombokVersion", "1.18.20")
    set("materialIconsVersion", "0.7.0")
    set("micrometerVersion", "1.7.2")
    set("modelmapperVersion", "2.4.4")
    set("postgresqlVersion", "42.2.23")
    set("restAssuredVersion", "4.4.0")
    set("servletApiVersion", "4.0.1")
    set("springBootVersion", "2.5.2")
    set("springJdbcVersion", "5.3.9")
    set("springSecurityVersion", "5.5.1")
    set("thymeleafDialectVersion", "2.5.3")
    set("thymeleafExtrasVersion", "3.0.4.RELEASE")
    set("webjarsLocatorVersion", "0.47")
  }
}

val javaVersion: JavaVersion = JavaVersion.VERSION_11
val dependencyVersions = listOf(
  "org.junit.jupiter:junit-jupiter:${extra["junitVersion"]}",
  "org.junit.jupiter:junit-jupiter-api:${extra["junitVersion"]}",
  "org.slf4j:slf4j-api:1.7.31",
  "org.mockito:mockito-core:${extra["mockitoVersion"]}",
  "org.junit:junit-bom:${extra["junitVersion"]}",
  "junit:junit:4.13.2",
  "jakarta.xml.bind:jakarta.xml.bind-api:2.3.3",
  "jakarta.activation:jakarta.activation-api:2.0.1",
  "org.jboss.logging:jboss-logging:3.4.2.Final",
  "net.bytebuddy:byte-buddy:1.11.7",
  "org.javassist:javassist:3.28.0-GA",
  "org.webjars:jquery:3.6.0",
  "commons-io:commons-io:2.11.0"
)
val dependencyGroupVersions = mapOf(
  "org.springframework" to "5.3.7",
  "org.springframework.security" to extra["springSecurityVersion"] as String,
  "org.springframework.boot" to extra["springBootVersion"] as String,
  "com.fasterxml.jackson.core" to extra["jacksonVersion"] as String,
  "com.fasterxml.jackson.datatype" to extra["jacksonVersion"] as String,
  "com.fasterxml.jackson.module" to extra["jacksonVersion"] as String
)

plugins {
  id("java-library")
  id("org.siouan.frontend-jdk11") version "5.2.0"
  id("org.springframework.boot") version "2.5.2"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

allprojects {
  group = "rocks.metaldetector"

  repositories {
    mavenCentral()
  }
}

springBoot {
  mainClass.set("rocks.metaldetector.MetalDetectorApplication")
}

dependencies {
  developmentOnly("org.springframework.boot:spring-boot-devtools")
}

subprojects {
  project.apply(plugin = "java-library")
  project.apply(plugin = "io.spring.dependency-management")
  project.apply(plugin = "jacoco")

  configurations {
    all {
      resolutionStrategy {
        failOnVersionConflict()
        force(dependencyVersions)
            eachDependency {
              val forcedVersion = dependencyGroupVersions[requested.group]
              if (forcedVersion != null) {
                useVersion(forcedVersion)
              }
            }
        cacheDynamicVersionsFor(0, "seconds")
      }
    }
  }

  dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-configuration-processor")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:${rootProject.extra["jacksonVersion"]}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${rootProject.extra["jacksonVersion"]}")
    implementation("org.projectlombok:lombok:${rootProject.extra["lombokVersion"]}")

    annotationProcessor("org.projectlombok:lombok:${rootProject.extra["lombokVersion"]}")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
      exclude(group = "junit", module = "junit")
    }
    testImplementation("org.springframework.security:spring-security-test:${rootProject.extra["springSecurityVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter:${rootProject.extra["junitVersion"]}")
    testImplementation("org.junit.vintage:junit-vintage-engine:${rootProject.extra["junitVersion"]}")
    testImplementation("org.mockito:mockito-junit-jupiter:${rootProject.extra["mockitoVersion"]}")
    testImplementation("com.h2database:h2:${rootProject.extra["h2Version"]}")
  }

  dependencyManagement {
    dependencies {
      dependency("org.apache.commons:commons-text:${rootProject.extra["apacheCommonsTextVersion"]}")
    }
  }

  configure<JavaPluginConvention> {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
  }

  tasks {
    withType<Test> {
      useJUnitPlatform()
      testLogging.showStandardStreams = true
    }
    withType<JacocoReport> {
      reports {
        xml.isEnabled = true
        html.isEnabled = false
      }
    }
    withType<JavaCompile> {
      options.encoding = "UTF-8"
    }
  }
}

tasks {
  wrapper {
    gradleVersion = "7.0.2"
    distributionType = Wrapper.DistributionType.ALL
  }

  frontend {
    nodeDistributionProvided.set(false)
    nodeVersion.set("14.15.3")
    nodeDistributionUrlRoot.set("https://nodejs.org/dist/")
    nodeDistributionUrlPathPattern.set("vVERSION/node-vVERSION-ARCH.TYPE")
    nodeInstallDirectory.set(file("${projectDir}/node"))

    installScript.set("install")
    cleanScript.set("run clean")
    assembleScript.set("run assemble")
    checkScript.set("run test")

    packageJsonDirectory.set(file("${projectDir}/webapp/src/main/resources/static/ts"))
  }
}
