dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")

  implementation(project(":support"))

  testImplementation("org.apache.commons:commons-text")
}

description = "butler"