plugins {
    kotlin("jvm") version "1.9.22"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // The Golden Lock (Keep these exactly as they are)
    implementation("com.google.protobuf:protobuf-kotlin:3.25.3")
    implementation("io.grpc:grpc-protobuf:1.62.2")
    implementation("io.grpc:grpc-stub:1.62.2")
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")
    implementation("io.grpc:grpc-services:1.62.2") // Reflection
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")

    // The Armeria Engine
    implementation(platform("com.linecorp.armeria:armeria-bom:1.28.0"))
    implementation("com.linecorp.armeria:armeria-grpc")
    implementation("ch.qos.logback:logback-classic:1.5.3") // Armeria requires a logger to boot
}

application {
    mainClass.set("holocron.v1.ServerKt")
}

sourceSets {
    main {
        java { srcDir("src/main/gen") }
        kotlin { srcDir("src/main/gen") }
    }
}

