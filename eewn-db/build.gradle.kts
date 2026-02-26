plugins {
    java
}

dependencies {
    implementation(project(":eewn-model"))
    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}